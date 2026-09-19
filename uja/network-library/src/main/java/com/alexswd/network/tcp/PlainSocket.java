package com.alexswd.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * PlainSocket provides direct TCP socket connections by extending SocketBase.
 *
 * <p>
 * This class wraps the standard Java {@link Socket} class and delegates all operations to it. The
 * underlying socket is created on demand when needed.
 *
 * <p>
 * This is a minimal wrapper designed for clean delegation with virtually no overhead.
 *
 * @author Network Library
 * @version 1.0
 */
public class PlainSocket extends SocketBase {

  private Socket socket;

  /**
   * Constructs a new PlainSocket with no initial socket.
   *
   * <p>
   * The underlying socket will be created on demand when first needed.
   */
  public PlainSocket() {
    this.socket = null;
    this.connected = false;
    this.closed = false;
    this.inputShutdown = false;
    this.outputShutdown = false;
  }

  /**
   * Constructs a new PlainSocket wrapping an existing socket.
   *
   * @param socket the underlying socket to wrap
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public PlainSocket(Socket socket) {
    this.socket = socket;
    this.connected = socket != null && socket.isConnected();
    this.closed = socket != null && socket.isClosed();
    this.inputShutdown = false;
    this.outputShutdown = false;
  }

  /**
   * Returns the underlying socket, creating it if necessary.
   *
   * @return the underlying socket
   */
  private Socket getSocket() {
    if (socket == null) {
      socket = new Socket();
    }
    return socket;
  }

  // ============================================================================
  // Abstract Method Implementations
  // ============================================================================

  @Override
  protected void connectInternal(String host, int port, int timeoutMs) throws IOException {
    logger.debug("Establishing plain TCP connection to {}:{}", host, port);
    if (timeoutMs > 0) {
      getSocket().connect(new java.net.InetSocketAddress(host, port), timeoutMs);
    } else {
      getSocket().connect(new java.net.InetSocketAddress(host, port));
    }
  }

  @Override
  protected InputStream getUnderlyingInputStream() throws IOException {
    return getSocket().getInputStream();
  }

  @Override
  protected OutputStream getUnderlyingOutputStream() throws IOException {
    return getSocket().getOutputStream();
  }

  @Override
  protected void closeInternal() throws IOException {
    if (socket != null) {
      socket.close();
    }
  }

  // ============================================================================
  // Overridden State Methods
  // ============================================================================

  @Override
  public boolean isConnected() {
    return socket != null && socket.isConnected();
  }

  @Override
  public boolean isBound() {
    return socket != null && socket.isBound();
  }

  @Override
  public boolean isClosed() {
    return socket != null && socket.isClosed();
  }

  @Override
  public void shutdownInput() throws IOException {
    logger.debug("Shutting down input");
    if (socket != null && !socket.isClosed()) {
      socket.shutdownInput();
    }
    inputShutdown = true;
  }

  @Override
  public void shutdownOutput() throws IOException {
    logger.debug("Shutting down output");
    if (socket != null && !socket.isClosed()) {
      socket.shutdownOutput();
    }
    outputShutdown = true;
  }

  // ============================================================================
  // Overridden Configuration Methods
  // ============================================================================

  @Override
  public void setSoTimeout(int timeout) throws java.net.SocketException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout cannot be negative");
    }
    logger.debug("Setting SO_TIMEOUT to {} ms", timeout);
    getSocket().setSoTimeout(timeout);
    this.soTimeout = timeout;
  }

  @Override
  public int getSoTimeout() throws java.net.SocketException {
    return getSocket().getSoTimeout();
  }

  @Override
  public void setTcpNoDelay(boolean on) throws java.net.SocketException {
    logger.debug("Setting TCP_NODELAY to {}", on);
    getSocket().setTcpNoDelay(on);
    this.tcpNoDelay = on;
  }

  @Override
  public boolean getTcpNoDelay() throws java.net.SocketException {
    return getSocket().getTcpNoDelay();
  }

  @Override
  public void setKeepAlive(boolean on) throws java.net.SocketException {
    logger.debug("Setting KEEP_ALIVE to {}", on);
    getSocket().setKeepAlive(on);
    this.keepAlive = on;
  }

  @Override
  public boolean getKeepAlive() throws java.net.SocketException {
    return getSocket().getKeepAlive();
  }

  @Override
  public void setReuseAddress(boolean on) throws java.net.SocketException {
    logger.debug("Setting REUSE_ADDR to {}", on);
    getSocket().setReuseAddress(on);
    this.reuseAddress = on;
  }

  @Override
  public boolean getReuseAddress() throws java.net.SocketException {
    return getSocket().getReuseAddress();
  }

  @Override
  public void setReceiveBufferSize(int size) throws java.net.SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size must be greater than 0");
    }
    logger.debug("Setting receive buffer size to {} bytes", size);
    getSocket().setReceiveBufferSize(size);
    this.receiveBufferSize = size;
  }

  @Override
  public int getReceiveBufferSize() throws java.net.SocketException {
    return getSocket().getReceiveBufferSize();
  }

  @Override
  public void setSendBufferSize(int size) throws java.net.SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size must be greater than 0");
    }
    logger.debug("Setting send buffer size to {} bytes", size);
    getSocket().setSendBufferSize(size);
    this.sendBufferSize = size;
  }

  @Override
  public int getSendBufferSize() throws java.net.SocketException {
    return getSocket().getSendBufferSize();
  }

  @Override
  public void setTrafficClass(int tc) throws java.net.SocketException {
    logger.debug("Setting traffic class to {}", tc);
    getSocket().setTrafficClass(tc);
    this.trafficClass = tc;
  }

  @Override
  public int getTrafficClass() throws java.net.SocketException {
    return getSocket().getTrafficClass();
  }

  @Override
  public void setOOBInline(boolean on) throws java.net.SocketException {
    logger.debug("Setting OOB inline to {}", on);
    getSocket().setOOBInline(on);
    this.oobInline = on;
  }

  @Override
  public boolean getOOBInline() throws java.net.SocketException {
    return getSocket().getOOBInline();
  }

  @Override
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    logger.debug("Setting performance preferences - connectionTime: {}, latency: {}, bandwidth: {}",
        connectionTime, latency, bandwidth);
    getSocket().setPerformancePreferences(connectionTime, latency, bandwidth);
  }

  // ============================================================================
  // Overridden Socket Information Methods
  // ============================================================================

  @Override
  public InetAddress getInetAddress() {
    return socket != null ? socket.getInetAddress() : null;
  }

  @Override
  public int getPort() {
    return socket != null ? socket.getPort() : 0;
  }

  @Override
  public InetAddress getLocalAddress() {
    return socket != null ? socket.getLocalAddress() : null;
  }

  @Override
  public int getLocalPort() {
    return socket != null ? socket.getLocalPort() : -1;
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    return socket != null ? socket.getRemoteSocketAddress() : null;
  }

  @Override
  public SocketAddress getLocalSocketAddress() {
    return socket != null ? socket.getLocalSocketAddress() : null;
  }

  @Override
  public java.nio.channels.SocketChannel getChannel() {
    return socket != null ? socket.getChannel() : null;
  }

  @Override
  public void sendUrgentData(int data) throws IOException {
    logger.debug("Sending urgent data: {}", data);
    getSocket().sendUrgentData(data);
  }

  @Override
  public String toString() {
    if (socket == null) {
      return "PlainSocket[uninitialized]";
    }
    return String.format("PlainSocket[addr=%s,port=%d,localport=%d]", socket.getInetAddress(),
        socket.getPort(), socket.getLocalPort());
  }
}
