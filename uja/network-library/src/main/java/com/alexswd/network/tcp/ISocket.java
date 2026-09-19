package com.alexswd.network.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

/**
 * ISocket interface defines the contract for socket operations.
 *
 * <p>
 * This interface abstracts the major public methods from {@link java.net.Socket} class, organized
 * into logical categories: connection, streams, state, shutdown, configuration, and information
 * methods.
 *
 * @author Network Library
 * @version 1.0
 */
public interface ISocket {

  // ============================================================================
  // Connection Methods
  // ============================================================================

  /**
   * Connects this socket to the server with a specified timeout of zero.
   *
   * @param endpoint the SocketAddress to connect to
   * @throws IOException if an I/O error occurs during connection
   * @throws java.net.SocketException if the socket is already connected
   */
  void connect(SocketAddress endpoint) throws IOException;

  /**
   * Connects this socket to the server with a specified timeout value.
   *
   * <p>
   * A timeout of zero is interpreted as an infinite timeout. The connection will then block until
   * established or an error occurs.
   *
   * @param endpoint the SocketAddress to connect to
   * @param timeout the time in milliseconds to wait for the connection
   * @throws IOException if an I/O error occurs during connection
   * @throws java.net.SocketTimeoutException if timeout expires before connecting
   */
  void connect(SocketAddress endpoint, int timeout) throws IOException;

  /**
   * Binds the socket to a local address.
   *
   * <p>
   * If the address is null, the socket will be bound to an available local address on any
   * interface.
   *
   * @param bindpoint the SocketAddress to bind to
   * @throws IOException if an I/O error occurs during binding
   * @throws java.net.SocketException if the socket is already bound
   */
  void bind(SocketAddress bindpoint) throws IOException;

  // ============================================================================
  // Streams Methods
  // ============================================================================

  /**
   * Returns an input stream for this socket.
   *
   * <p>
   * If this socket has an associated channel then the resulting input stream delegates all of its
   * operations to the channel.
   *
   * @return an InputStream for reading from this socket
   * @throws IOException if an I/O error occurs when creating the input stream
   */
  InputStream getInputStream() throws IOException;

  /**
   * Returns an output stream for this socket.
   *
   * <p>
   * If this socket has an associated channel then the resulting output stream delegates all of its
   * operations to the channel.
   *
   * @return an OutputStream for writing to this socket
   * @throws IOException if an I/O error occurs when creating the output stream
   */
  OutputStream getOutputStream() throws IOException;

  // ============================================================================
  // Connection State Methods
  // ============================================================================

  /**
   * Returns the connection state of the socket.
   *
   * <p>
   * Note: Closing a socket doesn't clear its connection state, which means this method will return
   * true for a closed socket.
   *
   * @return true if the socket has been connected to a remote peer
   */
  boolean isConnected();

  /**
   * Returns the binding state of the socket.
   *
   * @return true if the socket has been bound to an address
   */
  boolean isBound();

  /**
   * Returns whether the socket is closed.
   *
   * @return true if the socket has been closed
   */
  boolean isClosed();

  /**
   * Returns whether the input of the socket is shutdown.
   *
   * @return true if the socket has been shutdown for input
   */
  boolean isInputShutdown();

  /**
   * Returns whether the output of the socket is shutdown.
   *
   * @return true if the socket has been shutdown for output
   */
  boolean isOutputShutdown();

  // ============================================================================
  // Shutdown Methods
  // ============================================================================

  /**
   * Closes this socket.
   *
   * <p>
   * Any thread currently blocked in an I/O operation upon this socket will throw a SocketException.
   *
   * @throws IOException if an I/O error occurs when closing this socket
   */
  void close() throws IOException;

  /**
   * Disables the input stream for this socket.
   *
   * <p>
   * Any subsequent attempts to read from this socket's input stream will throw an EOFException. If
   * this socket is closed before shutdownInput is called, this method has no effect.
   *
   * @throws IOException if an I/O error occurs when shutting down the input
   */
  void shutdownInput() throws IOException;

  /**
   * Disables the output stream for this socket.
   *
   * <p>
   * Any subsequent attempts to write to this socket's output stream will throw an IOException. If
   * this socket is closed before shutdownOutput is called, this method has no effect.
   *
   * @throws IOException if an I/O error occurs when shutting down the output
   */
  void shutdownOutput() throws IOException;

  // ============================================================================
  // Configuration Methods
  // ============================================================================

  /**
   * Sets the timeout for blocking socket operations.
   *
   * <p>
   * With this option set to a non-zero timeout, a call to read() or accept() will block for at most
   * this long. If the timeout expires, a SocketTimeoutException is raised, though the Socket is
   * still valid.
   *
   * @param timeout the specified timeout, in milliseconds. A timeout of zero is interpreted as an
   *        infinite timeout
   * @throws SocketException if there is an error in the underlying protocol
   */
  void setSoTimeout(int timeout) throws SocketException;

  /**
   * Gets the timeout value for blocking socket operations.
   *
   * @return the timeout value, in milliseconds
   * @throws SocketException if there is an error in the underlying protocol
   */
  int getSoTimeout() throws SocketException;

  /**
   * Enables/disables the SO_TCPNODELAY socket option.
   *
   * <p>
   * Setting TCP_NODELAY to true disables the Nagle algorithm. This means that segments are always
   * sent as soon as possible, even if there is only a small amount of data.
   *
   * @param on true to enable TCP_NODELAY, false to disable
   * @throws SocketException if there is an error in the underlying protocol
   */
  void setTcpNoDelay(boolean on) throws SocketException;

  /**
   * Gets the SO_TCPNODELAY socket option.
   *
   * @return true if SO_TCPNODELAY is enabled
   * @throws SocketException if there is an error in the underlying protocol
   */
  boolean getTcpNoDelay() throws SocketException;

  /**
   * Enables/disables the SO_KEEPALIVE socket option.
   *
   * <p>
   * When the SO_KEEPALIVE option is set, the socket will monitor for idle connections by sending
   * periodic probes.
   *
   * @param on true to enable SO_KEEPALIVE, false to disable
   * @throws SocketException if there is an error in the underlying protocol
   */
  void setKeepAlive(boolean on) throws SocketException;

  /**
   * Gets the SO_KEEPALIVE socket option.
   *
   * @return true if SO_KEEPALIVE is enabled
   * @throws SocketException if there is an error in the underlying protocol
   */
  boolean getKeepAlive() throws SocketException;

  /**
   * Enables/disables the SO_REUSEADDR socket option.
   *
   * <p>
   * When the SO_REUSEADDR option is enabled, a socket may be bound to a port in TIME_WAIT state.
   *
   * @param on true to enable SO_REUSEADDR, false to disable
   * @throws SocketException if there is an error in the underlying protocol
   */
  void setReuseAddress(boolean on) throws SocketException;

  /**
   * Gets the SO_REUSEADDR socket option.
   *
   * @return true if SO_REUSEADDR is enabled
   * @throws SocketException if there is an error in the underlying protocol
   */
  boolean getReuseAddress() throws SocketException;

  /**
   * Sets the SO_RCVBUF socket option. The SO_RCVBUF option determines the buffer size used by the
   * platform for input on this socket.
   *
   * @param size the size to set to. This value must be greater than 0
   * @throws IllegalArgumentException if the value is 0 or negative
   * @throws SocketException if there is an error in the underlying protocol
   */
  void setReceiveBufferSize(int size) throws SocketException;

  /**
   * Gets the SO_RCVBUF socket option.
   *
   * @return the value of the SO_RCVBUF socket option
   * @throws SocketException if there is an error in the underlying protocol
   */
  int getReceiveBufferSize() throws SocketException;

  /**
   * Sets the SO_SNDBUF socket option. The SO_SNDBUF option determines the buffer size used by the
   * platform for output on this socket.
   *
   * @param size the size to set to. This value must be greater than 0
   * @throws IllegalArgumentException if the value is 0 or negative
   * @throws SocketException if there is an error in the underlying protocol
   */
  void setSendBufferSize(int size) throws SocketException;

  /**
   * Gets the SO_SNDBUF socket option.
   *
   * @return the value of the SO_SNDBUF socket option
   * @throws SocketException if there is an error in the underlying protocol
   */
  int getSendBufferSize() throws SocketException;

  /**
   * Sets the traffic class (or type-of-service) octet in the IP header for packets sent from this
   * Socket. As the underlying network implementation may ignore this value applications should
   * consider it a hint.
   *
   * @param tc an int value for the traffic class
   * @throws SocketException if there is an error setting the traffic class
   */
  void setTrafficClass(int tc) throws SocketException;

  /**
   * Gets the traffic class setting in this Socket's IP header, for packets sent from this Socket.
   *
   * @return the traffic class of type int
   * @throws SocketException if there is an error getting the traffic class
   */
  int getTrafficClass() throws SocketException;

  /**
   * Enables/disables the SO_OOBINLINE socket option.
   *
   * <p>
   * When this option is enabled, out-of-band (urgent) data received on the socket is received
   * inline with regular data.
   *
   * @param on true to enable SO_OOBINLINE, false to disable
   * @throws SocketException if there is an error in the underlying protocol
   */
  void setOOBInline(boolean on) throws SocketException;

  /**
   * Gets the SO_OOBINLINE socket option.
   *
   * @return true if SO_OOBINLINE is enabled
   * @throws SocketException if there is an error in the underlying protocol
   */
  boolean getOOBInline() throws SocketException;

  /**
   * Sets performance preferences for this Socket.
   *
   * <p>
   * Sockets use the TCP/IP protocol by default. Some implementations may offer alternative
   * protocols which have different performance characteristics than TCP/IP. This method allows the
   * application to express its own preferences as to how these tradeoffs should be made when the
   * implementation chooses from the available protocols.
   *
   * @param connectionTime an int expressing the relative importance of a short connection time
   * @param latency an int expressing the relative importance of low latency
   * @param bandwidth an int expressing the relative importance of high bandwidth
   */
  void setPerformancePreferences(int connectionTime, int latency, int bandwidth);

  // ============================================================================
  // Socket Information Methods
  // ============================================================================

  /**
   * Returns the address of the endpoint this socket is connected to, or null if it is not connected
   * yet.
   *
   * @return an InetAddress representing the remote IP address
   */
  InetAddress getInetAddress();

  /**
   * Returns the remote port number to which this socket is connected, or 0 if the socket is not
   * connected yet.
   *
   * @return the remote port to which the socket is connected
   */
  int getPort();

  /**
   * Returns the local address to which the socket is bound, or the wildcard address if the socket
   * is not bound yet.
   *
   * @return the local address to which the socket is bound
   */
  InetAddress getLocalAddress();

  /**
   * Returns the local port number to which this socket is bound. Returns -1 if the socket is not
   * bound yet.
   *
   * @return the local port to which the socket is bound
   */
  int getLocalPort();

  /**
   * Returns the remote address to which this socket is connected, or null if it is not connected
   * yet.
   *
   * @return a SocketAddress representing the remote socket address
   */
  SocketAddress getRemoteSocketAddress();

  /**
   * Returns the local address and port of this socket as a SocketAddress, or null if the socket is
   * not bound.
   *
   * @return a SocketAddress representing the local socket address
   */
  SocketAddress getLocalSocketAddress();

  /**
   * Returns the unique SocketChannel object associated with this socket, if any.
   *
   * <p>
   * A socket will have a channel if, and only if, the channel itself was created via the
   * SocketChannel.open() or ServerSocketChannel.accept() methods.
   *
   * @return the socket channel associated with this socket, or null if this socket was not created
   *         for a channel
   */
  SocketChannel getChannel();

  // ============================================================================
  // Other Methods
  // ============================================================================

  /**
   * Sends one byte of urgent data on the socket. To send more than one byte of urgent data, the
   * application must repeat the call to sendUrgentData.
   *
   * <p>
   * If this socket has an associated channel then the underlying SocketChannel.write(byte[]) is
   * called to send the urgent byte.
   *
   * @param data the byte of urgent data to be sent
   * @throws IOException if an I/O error occurs
   */
  void sendUrgentData(int data) throws IOException;

  /**
   * Converts this socket to a String.
   *
   * @return a string representation of this socket
   */
  @Override
  String toString();
}
