package com.alexswd.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SocketBase is an abstract base class that provides common socket functionality for ISocket
 * implementations.
 *
 * <p>
 * This class encapsulates common socket operations including connection management, stream access,
 * state tracking, socket configuration, and socket information retrieval. Subclasses need only
 * implement abstract methods for connection logic and stream access.
 *
 * <p>
 * Thread-safety: This class is not thread-safe. External synchronization is required if multiple
 * threads access the same socket instance.
 *
 * @author Network Library
 * @version 1.0
 */
public abstract class SocketBase implements ISocket {

  protected static final Logger logger = LoggerFactory.getLogger(SocketBase.class);

  // ============================================================================
  // Protected Fields - Connection State
  // ============================================================================

  /** Whether this socket is currently connected to a remote peer. */
  protected boolean connected;

  /** Whether this socket has been closed. */
  protected boolean closed;

  /** Whether the input stream has been shutdown. */
  protected boolean inputShutdown;

  /** Whether the output stream has been shutdown. */
  protected boolean outputShutdown;

  // ============================================================================
  // Protected Fields - Socket Options
  // ============================================================================

  /** Socket timeout in milliseconds (SO_TIMEOUT). */
  protected int soTimeout;

  /** TCP no delay option (TCP_NODELAY). */
  protected boolean tcpNoDelay;

  /** Keep-alive option (SO_KEEPALIVE). */
  protected boolean keepAlive;

  /** Reuse address option (SO_REUSEADDR). */
  protected boolean reuseAddress;

  /** Receive buffer size in bytes (SO_RCVBUF). */
  protected int receiveBufferSize;

  /** Send buffer size in bytes (SO_SNDBUF). */
  protected int sendBufferSize;

  /** Traffic class (Type of Service) in IP header. */
  protected int trafficClass;

  /** Out-of-band inline option (SO_OOBINLINE). */
  protected boolean oobInline;

  // ============================================================================
  // Abstract Methods
  // ============================================================================

  /**
   * Establishes an internal connection to the specified host and port.
   *
   * <p>
   * Subclasses must implement this method to establish the actual connection. This method is called
   * by the public {@link #connect(SocketAddress)} and {@link #connect(SocketAddress, int)} methods
   * after extracting the host and port.
   *
   * @param host the hostname or IP address to connect to
   * @param port the port number to connect to
   * @param timeoutMs the connection timeout in milliseconds (0 for infinite)
   * @throws IOException if an I/O error occurs during connection
   */
  protected abstract void connectInternal(String host, int port, int timeoutMs) throws IOException;

  /**
   * Returns the underlying input stream for this socket.
   *
   * <p>
   * Subclasses must implement this method to return the actual input stream, which may be obtained
   * from an underlying socket or other source.
   *
   * @return the input stream
   * @throws IOException if an I/O error occurs
   */
  protected abstract InputStream getUnderlyingInputStream() throws IOException;

  /**
   * Returns the underlying output stream for this socket.
   *
   * <p>
   * Subclasses must implement this method to return the actual output stream, which may be obtained
   * from an underlying socket or other source.
   *
   * @return the output stream
   * @throws IOException if an I/O error occurs
   */
  protected abstract OutputStream getUnderlyingOutputStream() throws IOException;

  /**
   * Closes the underlying socket and releases all resources.
   *
   * <p>
   * Subclasses must implement this method to perform the actual close operation on the underlying
   * socket or connection.
   *
   * @throws IOException if an I/O error occurs during close
   */
  protected abstract void closeInternal() throws IOException;

  // ============================================================================
  // Connection Methods
  // ============================================================================

  /**
   * Connects this socket to the specified endpoint with a default timeout of zero (infinite).
   *
   * @param endpoint the SocketAddress to connect to
   * @throws IOException if an I/O error occurs during connection
   * @throws IllegalArgumentException if endpoint is null
   */
  @Override
  public void connect(SocketAddress endpoint) throws IOException {
    connect(endpoint, 0);
  }

  /**
   * Connects this socket to the specified endpoint with the given timeout.
   *
   * <p>
   * A timeout of zero is interpreted as an infinite timeout. The connection will block until
   * established or an error occurs.
   *
   * @param endpoint the SocketAddress to connect to
   * @param timeout the connection timeout in milliseconds (0 = infinite)
   * @throws IOException if an I/O error occurs during connection
   * @throws IllegalArgumentException if endpoint is null or timeout is negative
   */
  @Override
  public void connect(SocketAddress endpoint, int timeout) throws IOException {
    if (endpoint == null) {
      throw new IllegalArgumentException("Endpoint cannot be null");
    }
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout cannot be negative");
    }

    if (!(endpoint instanceof InetSocketAddress)) {
      throw new IOException("Unsupported endpoint type: " + endpoint.getClass().getName());
    }

    InetSocketAddress inetAddr = (InetSocketAddress) endpoint;
    String host = inetAddr.getHostName();
    int port = inetAddr.getPort();

    if (host == null || host.isEmpty()) {
      throw new IOException("Host cannot be null or empty");
    }

    if (port < 0 || port > 65535) {
      throw new IOException("Invalid port: " + port);
    }

    logger.debug("Connecting to {}:{} with timeout {} ms", host, port, timeout);
    connectInternal(host, port, timeout);
    this.connected = true;
  }

  /**
   * Binds this socket to the specified local address.
   *
   * <p>
   * This is not implemented by default and throws UnsupportedOperationException. Subclasses may
   * override this method if binding is supported.
   *
   * @param bindpoint the SocketAddress to bind to
   * @throws IOException always, as bind is not supported by default
   */
  @Override
  public void bind(SocketAddress bindpoint) throws IOException {
    throw new UnsupportedOperationException("bind operation is not supported");
  }

  // ============================================================================
  // Streams Methods
  // ============================================================================

  /**
   * Returns the input stream for reading from this socket.
   *
   * @return an InputStream for reading from this socket
   * @throws IOException if an I/O error occurs
   */
  @Override
  public InputStream getInputStream() throws IOException {
    logger.debug("Getting input stream");
    return getUnderlyingInputStream();
  }

  /**
   * Returns the output stream for writing to this socket.
   *
   * @return an OutputStream for writing to this socket
   * @throws IOException if an I/O error occurs
   */
  @Override
  public OutputStream getOutputStream() throws IOException {
    logger.debug("Getting output stream");
    return getUnderlyingOutputStream();
  }

  // ============================================================================
  // Connection State Methods
  // ============================================================================

  /**
   * Returns whether this socket is connected to a remote peer.
   *
   * @return true if the socket is connected
   */
  @Override
  public boolean isConnected() {
    return connected;
  }

  /**
   * Returns whether this socket is bound to a local address.
   *
   * <p>
   * Note: The default implementation returns false. Subclasses should override if they support
   * binding.
   *
   * @return false as binding is not supported by default
   */
  @Override
  public boolean isBound() {
    return false;
  }

  /**
   * Returns whether this socket is closed.
   *
   * @return true if the socket is closed
   */
  @Override
  public boolean isClosed() {
    return closed;
  }

  /**
   * Returns whether the input stream of this socket has been shutdown.
   *
   * @return true if the input stream is shutdown
   */
  @Override
  public boolean isInputShutdown() {
    return inputShutdown;
  }

  /**
   * Returns whether the output stream of this socket has been shutdown.
   *
   * @return true if the output stream is shutdown
   */
  @Override
  public boolean isOutputShutdown() {
    return outputShutdown;
  }

  // ============================================================================
  // Shutdown Methods
  // ============================================================================

  /**
   * Closes this socket and releases all associated resources.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    logger.debug("Closing socket");
    if (!closed) {
      closeInternal();
      closed = true;
    }
  }

  /**
   * Shuts down the input stream for this socket.
   *
   * <p>
   * After calling this method, subsequent read operations will throw an EOFException.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void shutdownInput() throws IOException {
    logger.debug("Shutting down input");
    inputShutdown = true;
  }

  /**
   * Shuts down the output stream for this socket.
   *
   * <p>
   * After calling this method, subsequent write operations will throw an IOException.
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void shutdownOutput() throws IOException {
    logger.debug("Shutting down output");
    outputShutdown = true;
  }

  // ============================================================================
  // Configuration Methods - Timeout
  // ============================================================================

  /**
   * Sets the socket timeout for blocking operations.
   *
   * <p>
   * With this option set to a non-zero timeout, socket operations will block for at most this long.
   * A timeout of zero means infinite timeout.
   *
   * @param timeout the timeout in milliseconds (0 = infinite)
   * @throws IllegalArgumentException if timeout is negative
   * @throws SocketException if an error occurs
   */
  @Override
  public void setSoTimeout(int timeout) throws SocketException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout cannot be negative");
    }
    logger.debug("Setting SO_TIMEOUT to {} ms", timeout);
    this.soTimeout = timeout;
  }

  /**
   * Returns the socket timeout value.
   *
   * @return the timeout in milliseconds
   * @throws SocketException if an error occurs
   */
  @Override
  public int getSoTimeout() throws SocketException {
    return soTimeout;
  }

  // ============================================================================
  // Configuration Methods - TCP Options
  // ============================================================================

  /**
   * Enables or disables TCP_NODELAY (Nagle's algorithm).
   *
   * <p>
   * When TCP_NODELAY is enabled, segments are sent as soon as possible, even with small amounts of
   * data.
   *
   * @param on true to disable Nagle's algorithm
   * @throws SocketException if an error occurs
   */
  @Override
  public void setTcpNoDelay(boolean on) throws SocketException {
    logger.debug("Setting TCP_NODELAY to {}", on);
    this.tcpNoDelay = on;
  }

  /**
   * Returns whether TCP_NODELAY is enabled.
   *
   * @return true if TCP_NODELAY is enabled
   * @throws SocketException if an error occurs
   */
  @Override
  public boolean getTcpNoDelay() throws SocketException {
    return tcpNoDelay;
  }

  /**
   * Enables or disables SO_KEEPALIVE.
   *
   * <p>
   * When enabled, the socket will monitor for idle connections by sending periodic probes.
   *
   * @param on true to enable keep-alive
   * @throws SocketException if an error occurs
   */
  @Override
  public void setKeepAlive(boolean on) throws SocketException {
    logger.debug("Setting KEEP_ALIVE to {}", on);
    this.keepAlive = on;
  }

  /**
   * Returns whether SO_KEEPALIVE is enabled.
   *
   * @return true if keep-alive is enabled
   * @throws SocketException if an error occurs
   */
  @Override
  public boolean getKeepAlive() throws SocketException {
    return keepAlive;
  }

  /**
   * Enables or disables SO_REUSEADDR.
   *
   * <p>
   * When enabled, a socket may be bound to a port in TIME_WAIT state.
   *
   * @param on true to enable address reuse
   * @throws SocketException if an error occurs
   */
  @Override
  public void setReuseAddress(boolean on) throws SocketException {
    logger.debug("Setting REUSE_ADDR to {}", on);
    this.reuseAddress = on;
  }

  /**
   * Returns whether SO_REUSEADDR is enabled.
   *
   * @return true if address reuse is enabled
   * @throws SocketException if an error occurs
   */
  @Override
  public boolean getReuseAddress() throws SocketException {
    return reuseAddress;
  }

  // ============================================================================
  // Configuration Methods - Buffer Sizes
  // ============================================================================

  /**
   * Sets the SO_RCVBUF (receive buffer size).
   *
   * @param size the receive buffer size in bytes (must be positive)
   * @throws IllegalArgumentException if size is not positive
   * @throws SocketException if an error occurs
   */
  @Override
  public void setReceiveBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size must be greater than 0");
    }
    logger.debug("Setting receive buffer size to {} bytes", size);
    this.receiveBufferSize = size;
  }

  /**
   * Returns the SO_RCVBUF (receive buffer size).
   *
   * @return the receive buffer size in bytes
   * @throws SocketException if an error occurs
   */
  @Override
  public int getReceiveBufferSize() throws SocketException {
    return receiveBufferSize;
  }

  /**
   * Sets the SO_SNDBUF (send buffer size).
   *
   * @param size the send buffer size in bytes (must be positive)
   * @throws IllegalArgumentException if size is not positive
   * @throws SocketException if an error occurs
   */
  @Override
  public void setSendBufferSize(int size) throws SocketException {
    if (size <= 0) {
      throw new IllegalArgumentException("Buffer size must be greater than 0");
    }
    logger.debug("Setting send buffer size to {} bytes", size);
    this.sendBufferSize = size;
  }

  /**
   * Returns the SO_SNDBUF (send buffer size).
   *
   * @return the send buffer size in bytes
   * @throws SocketException if an error occurs
   */
  @Override
  public int getSendBufferSize() throws SocketException {
    return sendBufferSize;
  }

  // ============================================================================
  // Configuration Methods - Traffic and OOB
  // ============================================================================

  /**
   * Sets the traffic class (Type of Service) for packets sent from this socket.
   *
   * @param tc the traffic class value
   * @throws SocketException if an error occurs
   */
  @Override
  public void setTrafficClass(int tc) throws SocketException {
    logger.debug("Setting traffic class to {}", tc);
    this.trafficClass = tc;
  }

  /**
   * Returns the traffic class (Type of Service) for this socket.
   *
   * @return the traffic class value
   * @throws SocketException if an error occurs
   */
  @Override
  public int getTrafficClass() throws SocketException {
    return trafficClass;
  }

  /**
   * Enables or disables SO_OOBINLINE.
   *
   * <p>
   * When enabled, out-of-band (urgent) data is received inline with regular data.
   *
   * @param on true to enable out-of-band inline
   * @throws SocketException if an error occurs
   */
  @Override
  public void setOOBInline(boolean on) throws SocketException {
    logger.debug("Setting OOB inline to {}", on);
    this.oobInline = on;
  }

  /**
   * Returns whether SO_OOBINLINE is enabled.
   *
   * @return true if out-of-band inline is enabled
   * @throws SocketException if an error occurs
   */
  @Override
  public boolean getOOBInline() throws SocketException {
    return oobInline;
  }

  /**
   * Sets performance preferences for this socket.
   *
   * @param connectionTime preference for connection time
   * @param latency preference for low latency
   * @param bandwidth preference for high bandwidth
   */
  @Override
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    logger.debug("Setting performance preferences - connectionTime: {}, latency: {}, bandwidth: {}",
        connectionTime, latency, bandwidth);
  }

  // ============================================================================
  // Socket Information Methods
  // ============================================================================

  /**
   * Returns the remote address this socket is connected to.
   *
   * <p>
   * The default implementation returns null. Subclasses should override to provide actual remote
   * address information.
   *
   * @return the remote InetAddress, or null if not connected
   */
  @Override
  public InetAddress getInetAddress() {
    return null;
  }

  /**
   * Returns the remote port this socket is connected to.
   *
   * <p>
   * The default implementation returns 0. Subclasses should override to provide actual port
   * information.
   *
   * @return the remote port, or 0 if not connected
   */
  @Override
  public int getPort() {
    return 0;
  }

  /**
   * Returns the local address this socket is bound to.
   *
   * <p>
   * The default implementation returns null. Subclasses should override to provide actual local
   * address information.
   *
   * @return the local InetAddress, or null if not bound
   */
  @Override
  public InetAddress getLocalAddress() {
    return null;
  }

  /**
   * Returns the local port this socket is bound to.
   *
   * <p>
   * The default implementation returns -1. Subclasses should override to provide actual port
   * information.
   *
   * @return the local port, or -1 if not bound
   */
  @Override
  public int getLocalPort() {
    return -1;
  }

  /**
   * Returns the remote socket address.
   *
   * <p>
   * The default implementation returns null. Subclasses should override to provide actual remote
   * address information.
   *
   * @return a SocketAddress representing the remote endpoint, or null if not connected
   */
  @Override
  public SocketAddress getRemoteSocketAddress() {
    return null;
  }

  /**
   * Returns the local socket address.
   *
   * <p>
   * The default implementation returns null. Subclasses should override to provide actual local
   * address information.
   *
   * @return a SocketAddress representing the local endpoint, or null if not bound
   */
  @Override
  public SocketAddress getLocalSocketAddress() {
    return null;
  }

  /**
   * Returns the SocketChannel associated with this socket.
   *
   * <p>
   * The default implementation returns null. Subclasses may override if they support channels.
   *
   * @return the socket channel, or null if not associated with a channel
   */
  @Override
  public SocketChannel getChannel() {
    return null;
  }

  /**
   * Sends one byte of urgent data on the socket.
   *
   * @param data the byte of urgent data
   * @throws IOException if an I/O error occurs
   */
  @Override
  public void sendUrgentData(int data) throws IOException {
    logger.debug("Sending urgent data: {}", data);
  }

  /**
   * Returns a string representation of this socket.
   *
   * @return a string describing the socket
   */
  @Override
  public abstract String toString();
}
