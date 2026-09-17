package com.alexswd.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * TorSocket extends SocketBase and provides socket connection to target addresses via Tor SOCKS5
 * proxy on Linux.
 *
 * <p>
 * This class handles the complete SOCKS5 protocol handshake to establish a connection through the
 * Tor network. By default, it connects to a Tor SOCKS5 proxy at 127.0.0.1:9050.
 *
 * <p>
 * Example usage:
 *
 * <pre>
 * {@code
 * TorSocket socket = new TorSocket();
 * socket.connect(new InetSocketAddress("example.com", 80));
 * InputStream in = socket.getInputStream();
 * OutputStream out = socket.getOutputStream();
 * // Use socket for communication
 * socket.close();
 * }
 * </pre>
 *
 * @author Network Library
 * @version 1.0
 */
public class TorSocket extends SocketBase {

  // SOCKS5 protocol constants
  private static final byte SOCKS_VERSION = 0x05;
  private static final byte SOCKS_AUTH_NONE = 0x00;
  private static final byte SOCKS_CONNECT = 0x01;
  private static final byte SOCKS_ADDR_DOMAIN = 0x03;
  private static final byte SOCKS_STATUS_SUCCESS = 0x00;

  // Default Tor SOCKS5 proxy address and port
  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  private static final String DEFAULT_TOR_HOST = "127.0.0.1";
  private static final int DEFAULT_TOR_PORT = 9050;

  // Connection timeout in milliseconds
  private static final int DEFAULT_TIMEOUT_MS = 30000;

  // Socket and stream fields
  private Socket torSocket;
  private InputStream inputStream;
  private OutputStream outputStream;

  // Target address information
  private String targetHost;
  private int targetPort;
  private InetAddress remoteAddress;
  private InetAddress localAddress;
  private int localPort;

  /**
   * Constructs a TorSocket with the default Tor proxy configuration (127.0.0.1:9050).
   */
  public TorSocket() {
    this.connected = false;
    this.closed = false;
    this.inputShutdown = false;
    this.outputShutdown = false;
  }

  // ============================================================================
  // Abstract Method Implementations
  // ============================================================================

  @Override
  protected void connectInternal(String host, int port, int timeoutMs) throws IOException {
    this.targetHost = host;
    this.targetPort = port;

    int effectiveTimeout = (timeoutMs <= 0) ? DEFAULT_TIMEOUT_MS : timeoutMs;

    logger.debug("Connecting to target {}:{} via Tor proxy {}:{}", host, port, DEFAULT_TOR_HOST,
        DEFAULT_TOR_PORT);

    try {
      connectToTor(effectiveTimeout);
      performSOCKS5Handshake(effectiveTimeout);
      performSOCKS5Connect(host, port, effectiveTimeout);

      logger.info("Successfully connected to {}:{} via Tor", host, port);
    } catch (Exception e) {
      closeInternal();
      logger.error("Failed to connect to target {}:{} via Tor", host, port, e);
      throw e;
    }
  }

  @Override
  protected InputStream getUnderlyingInputStream() throws IOException {
    if (torSocket == null) {
      throw new IOException("Socket is not connected");
    }
    return inputStream;
  }

  @Override
  protected OutputStream getUnderlyingOutputStream() throws IOException {
    if (torSocket == null) {
      throw new IOException("Socket is not connected");
    }
    return outputStream;
  }

  @Override
  protected void closeInternal() throws IOException {
    if (torSocket != null) {
      try {
        torSocket.close();
        logger.debug("Tor socket closed");
      } catch (IOException e) {
        logger.warn("Error closing Tor socket", e);
      }
    }
    closed = true;
    connected = false;
    logger.info("TorSocket closed");
  }

  /**
   * Binds the socket to a local address.
   *
   * <p>
   * This operation is not supported for TorSocket as it connects through a Tor proxy.
   *
   * @param bindpoint the SocketAddress to bind to
   * @throws IOException if an error occurs
   */
  @Override
  public void bind(java.net.SocketAddress bindpoint) throws IOException {
    throw new UnsupportedOperationException("TorSocket does not support bind operation");
  }

  @Override
  public void shutdownInput() throws IOException {
    if (torSocket != null && !inputShutdown) {
      torSocket.shutdownInput();
      inputShutdown = true;
      logger.debug("Input stream shutdown");
    }
  }

  @Override
  public void shutdownOutput() throws IOException {
    if (torSocket != null && !outputShutdown) {
      torSocket.shutdownOutput();
      outputShutdown = true;
      logger.debug("Output stream shutdown");
    }
  }

  // ============================================================================
  // Overridden Configuration Methods
  // ============================================================================

  @Override
  public void setSoTimeout(int timeout) throws SocketException {
    if (torSocket != null) {
      torSocket.setSoTimeout(timeout);
    }
    this.soTimeout = timeout;
  }

  @Override
  public int getSoTimeout() throws SocketException {
    if (torSocket != null) {
      return torSocket.getSoTimeout();
    }
    return soTimeout;
  }

  @Override
  public void setTcpNoDelay(boolean on) throws SocketException {
    if (torSocket != null) {
      torSocket.setTcpNoDelay(on);
    }
    this.tcpNoDelay = on;
  }

  @Override
  public boolean getTcpNoDelay() throws SocketException {
    if (torSocket != null) {
      return torSocket.getTcpNoDelay();
    }
    return tcpNoDelay;
  }

  @Override
  public void setKeepAlive(boolean on) throws SocketException {
    if (torSocket != null) {
      torSocket.setKeepAlive(on);
    }
    this.keepAlive = on;
  }

  @Override
  public boolean getKeepAlive() throws SocketException {
    if (torSocket != null) {
      return torSocket.getKeepAlive();
    }
    return keepAlive;
  }

  @Override
  public void setReuseAddress(boolean on) throws SocketException {
    if (torSocket != null) {
      torSocket.setReuseAddress(on);
    }
    this.reuseAddress = on;
  }

  @Override
  public boolean getReuseAddress() throws SocketException {
    if (torSocket != null) {
      return torSocket.getReuseAddress();
    }
    return reuseAddress;
  }

  @Override
  public void setReceiveBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size must be greater than 0");
    }
    if (torSocket != null) {
      torSocket.setReceiveBufferSize(size);
    }
    this.receiveBufferSize = size;
  }

  @Override
  public int getReceiveBufferSize() throws SocketException {
    if (torSocket != null) {
      return torSocket.getReceiveBufferSize();
    }
    return receiveBufferSize;
  }

  @Override
  public void setSendBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size must be greater than 0");
    }
    if (torSocket != null) {
      torSocket.setSendBufferSize(size);
    }
    this.sendBufferSize = size;
  }

  @Override
  public int getSendBufferSize() throws SocketException {
    if (torSocket != null) {
      return torSocket.getSendBufferSize();
    }
    return sendBufferSize;
  }

  @Override
  public void setTrafficClass(int tc) throws SocketException {
    if (torSocket != null) {
      torSocket.setTrafficClass(tc);
    }
    this.trafficClass = tc;
  }

  @Override
  public int getTrafficClass() throws SocketException {
    if (torSocket != null) {
      return torSocket.getTrafficClass();
    }
    return trafficClass;
  }

  @Override
  public void setOOBInline(boolean on) throws SocketException {
    if (torSocket != null) {
      torSocket.setOOBInline(on);
    }
    this.oobInline = on;
  }

  @Override
  public boolean getOOBInline() throws SocketException {
    if (torSocket != null) {
      return torSocket.getOOBInline();
    }
    return oobInline;
  }

  @Override
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    if (torSocket != null) {
      torSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }
    logger.debug("Setting performance preferences - connectionTime: {}, latency: {}, bandwidth: {}",
        connectionTime, latency, bandwidth);
  }

  // ============================================================================
  // Overridden Socket Information Methods
  // ============================================================================

  @Override
  @SuppressFBWarnings("EI_EXPOSE_REP")
  public InetAddress getInetAddress() {
    return remoteAddress;
  }

  @Override
  public int getPort() {
    return targetPort;
  }

  @Override
  @SuppressFBWarnings("EI_EXPOSE_REP")
  public InetAddress getLocalAddress() {
    if (torSocket != null) {
      return torSocket.getLocalAddress();
    }
    return localAddress;
  }

  @Override
  public int getLocalPort() {
    if (torSocket != null) {
      return torSocket.getLocalPort();
    }
    return localPort;
  }

  @Override
  public java.net.SocketAddress getRemoteSocketAddress() {
    if (connected && targetHost != null) {
      return new InetSocketAddress(targetHost, targetPort);
    }
    return null;
  }

  @Override
  public java.net.SocketAddress getLocalSocketAddress() {
    if (torSocket != null) {
      return torSocket.getLocalSocketAddress();
    }
    return null;
  }

  @Override
  public java.nio.channels.SocketChannel getChannel() {
    return null;
  }

  @Override
  public void sendUrgentData(int data) throws IOException {
    if (torSocket != null) {
      torSocket.sendUrgentData(data);
    }
  }

  @Override
  public String toString() {
    return String.format("TorSocket(connected=%s, target=%s:%d, closed=%s)", connected, targetHost,
        targetPort, closed);
  }

  // ============================================================================
  // Private Helper Methods - SOCKS5 Protocol
  // ============================================================================

  /**
   * Establishes a TCP connection to the Tor SOCKS5 proxy.
   *
   * @param timeoutMs the connection timeout in milliseconds
   * @throws IOException if connection fails
   */
  private void connectToTor(int timeoutMs) throws IOException {
    logger.debug("Connecting to Tor proxy at {}:{}", DEFAULT_TOR_HOST, DEFAULT_TOR_PORT);

    torSocket = new Socket();
    try {
      torSocket.connect(new InetSocketAddress(DEFAULT_TOR_HOST, DEFAULT_TOR_PORT), timeoutMs);
      this.inputStream = torSocket.getInputStream();
      this.outputStream = torSocket.getOutputStream();
      this.localAddress = torSocket.getLocalAddress();
      this.localPort = torSocket.getLocalPort();

      logger.debug("Connected to Tor proxy");
    } catch (IOException e) {
      closeInternal();
      throw new IOException("Failed to connect to Tor proxy at " + DEFAULT_TOR_HOST + ":"
          + DEFAULT_TOR_PORT + ": " + e.getMessage(), e);
    }
  }

  /**
   * Performs the SOCKS5 authentication handshake with the Tor proxy.
   *
   * <p>
   * Sends the greeting message and validates the proxy response. This implementation uses no
   * authentication (SOCKS5 method 0x00).
   *
   * @param timeoutMs the operation timeout in milliseconds
   * @throws IOException if handshake fails
   */
  private void performSOCKS5Handshake(int timeoutMs) throws IOException {
    logger.debug("Performing SOCKS5 handshake");

    // Send greeting: [0x05, 0x01, 0x00]
    // - 0x05: SOCKS version 5
    // - 0x01: 1 authentication method offered
    // - 0x00: no authentication required
    byte[] greeting = {SOCKS_VERSION, 0x01, SOCKS_AUTH_NONE};

    outputStream.write(greeting);
    outputStream.flush();
    logger.debug("Sent SOCKS5 greeting");

    // Read response: should be [0x05, 0x00]
    // - 0x05: SOCKS version 5
    // - 0x00: selected authentication method (no auth)
    byte[] response = new byte[2];
    int bytesRead = readWithTimeout(inputStream, response, 0, 2, timeoutMs);

    if (bytesRead != 2) {
      throw new IOException(
          "Invalid SOCKS5 greeting response length: " + bytesRead + " (expected 2)");
    }

    if (response[0] != SOCKS_VERSION) {
      throw new IOException("Invalid SOCKS version in response: " + (response[0] & 0xFF)
          + " (expected " + (SOCKS_VERSION & 0xFF) + ")");
    }

    if (response[1] != SOCKS_AUTH_NONE) {
      throw new IOException(
          "Tor proxy requires unsupported authentication method: " + (response[1] & 0xFF));
    }

    logger.debug("SOCKS5 handshake completed successfully");
  }

  /**
   * Performs the SOCKS5 connect command to establish connection to the target address.
   *
   * <p>
   * Sends: [0x05, 0x01, 0x00, 0x03, len, domain, port_high, port_low] Receives and validates:
   * [0x05, status, 0x00, addr_type, ...]
   *
   * @param host the target hostname
   * @param port the target port
   * @param timeoutMs the operation timeout in milliseconds
   * @throws IOException if the connect command fails
   */
  private void performSOCKS5Connect(String host, int port, int timeoutMs) throws IOException {
    logger.debug("Sending SOCKS5 connect command for {}:{}", host, port);

    byte[] hostBytes = host.getBytes("UTF-8");

    if (hostBytes.length > 255) {
      throw new IOException("Hostname is too long: " + hostBytes.length + " bytes (max 255)");
    }

    // Build connect command
    // [0x05, 0x01, 0x00, 0x03, len, domain, port_high, port_low]
    int commandLength = 1 + 1 + 1 + 1 + 1 + hostBytes.length + 2;
    ByteBuffer buffer = ByteBuffer.allocate(commandLength);

    buffer.put(SOCKS_VERSION); // Version 5
    buffer.put(SOCKS_CONNECT); // Connect command
    buffer.put((byte) 0x00); // Reserved
    buffer.put(SOCKS_ADDR_DOMAIN); // Address type: domain name
    buffer.put((byte) hostBytes.length); // Domain length
    buffer.put(hostBytes); // Domain name
    buffer.put((byte) (port >> 8)); // Port high byte
    buffer.put((byte) (port & 0xFF)); // Port low byte

    outputStream.write(buffer.array());
    outputStream.flush();
    logger.debug("Sent SOCKS5 connect command");

    // Read response header: [0x05, status, 0x00, addr_type, ...]
    byte[] responseHeader = new byte[4];
    int bytesRead = readWithTimeout(inputStream, responseHeader, 0, 4, timeoutMs);

    if (bytesRead < 4) {
      throw new IOException(
          "Invalid SOCKS5 connect response: received " + bytesRead + " bytes, expected at least 4");
    }

    if (responseHeader[0] != SOCKS_VERSION) {
      throw new IOException("Invalid SOCKS version in response: " + (responseHeader[0] & 0xFF));
    }

    byte status = responseHeader[1];
    if (status != SOCKS_STATUS_SUCCESS) {
      String errorMsg = getSOCKS5ErrorMessage(status);
      throw new IOException("SOCKS5 connect failed: " + errorMsg + " (status 0x"
          + Integer.toHexString(status & 0xFF) + ")");
    }

    if (responseHeader[2] != 0x00) {
      throw new IOException("Invalid SOCKS5 response: reserved byte is not 0x00, got 0x"
          + Integer.toHexString(responseHeader[2] & 0xFF));
    }

    byte addrType = responseHeader[3];
    int remainingBytes = getRemainingResponseBytes(addrType);

    if (remainingBytes > 0) {
      byte[] remaining = new byte[remainingBytes];
      readWithTimeout(inputStream, remaining, 0, remainingBytes, timeoutMs);
    }

    logger.info("Successfully established SOCKS5 tunnel to {}:{}", host, port);

    // Store remote address information
    try {
      remoteAddress = InetAddress.getByName(host);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Could not resolve remote address for {}: {}", host, e.getMessage());
      }
    }
  }

  /**
   * Reads data from an input stream with a timeout.
   *
   * @param stream the input stream
   * @param buffer the byte array to read into
   * @param offset the offset in the buffer
   * @param length the number of bytes to read
   * @param timeoutMs the timeout in milliseconds
   * @return the number of bytes read
   * @throws IOException if an error occurs
   */
  private int readWithTimeout(InputStream stream, byte[] buffer, int offset, int length,
      int timeoutMs) throws IOException {
    if (buffer == null) {
      throw new IllegalArgumentException("Buffer cannot be null");
    }
    if (offset < 0 || length < 0 || offset + length > buffer.length) {
      throw new IllegalArgumentException("Invalid offset or length");
    }

    long startTime = System.currentTimeMillis();
    int bytesRead = 0;

    while (bytesRead < length) {
      if (timeoutMs > 0) {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= timeoutMs) {
          throw new SocketTimeoutException(
              "Read timeout after " + elapsed + "ms while reading SOCKS5 response");
        }
      }

      try {
        int available = stream.available();
        if (available > 0) {
          int toRead = Math.min(available, length - bytesRead);
          int read = stream.read(buffer, offset + bytesRead, toRead);
          if (read == -1) {
            throw new IOException("Unexpected end of stream from Tor proxy");
          }
          bytesRead += read;
        } else {
          Thread.sleep(10);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Thread interrupted during SOCKS5 handshake", e);
      }
    }

    return bytesRead;
  }

  /**
   * Gets the number of remaining bytes in a SOCKS5 response based on address type.
   *
   * @param addrType the address type byte
   * @return the number of remaining bytes to read
   */
  private int getRemainingResponseBytes(byte addrType) {
    switch (addrType) {
      case 0x01: // IPv4 address
        return 6; // 4 bytes address + 2 bytes port
      case 0x03: // Domain name
        // For domain name, first read 1 byte for domain length, then domain + 2 bytes port
        // Maximum 1 + 255 + 2 = 258 bytes, but we cap at 260 to be safe
        return 260;
      case 0x04: // IPv6 address
        return 18; // 16 bytes address + 2 bytes port
      default:
        return 0;
    }
  }

  /**
   * Gets a human-readable error message for a SOCKS5 status code.
   *
   * @param status the SOCKS5 status code
   * @return a descriptive error message
   */
  private String getSOCKS5ErrorMessage(byte status) {
    switch (status) {
      case 0x00:
        return "Success";
      case 0x01:
        return "General SOCKS server failure";
      case 0x02:
        return "Connection not allowed by ruleset";
      case 0x03:
        return "Network unreachable";
      case 0x04:
        return "Host unreachable";
      case 0x05:
        return "Connection refused";
      case 0x06:
        return "TTL expired";
      case 0x07:
        return "Command not supported";
      case 0x08:
        return "Address type not supported";
      default:
        return "Unknown error";
    }
  }
}
