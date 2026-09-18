package com.alexswd.network.http;

import com.alexswd.network.tcp.ISocket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * HTTP client for making HTTP requests.
 */
public class HttpClient {
  private final ISocket socket;

  /**
   * Creates a new HTTP client.
   */
  public HttpClient() {
    this.socket = null;
  }

  /**
   * Creates a new HTTP client with the specified socket.
   *
   * @param socket the socket to use for HTTP communication
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public HttpClient(ISocket socket) {
    this.socket = socket;
  }

  /**
   * Sends an HTTP GET request to the specified URL.
   *
   * @param url the URL to request (e.g., "http://example.com/path")
   * @return the HTTP response with status and body
   * @throws IOException if an I/O error occurs
   * @throws IllegalArgumentException if the URL is invalid
   */
  public HttpResponse get(String url) throws IOException {
    // Parse URL to extract host, port, and path
    URL urlObj = new URL(url);
    String host = urlObj.getHost();
    int port = urlObj.getPort();
    if (port == -1) {
      port = 80;
    }
    String path = urlObj.getPath();
    if (path == null || path.isEmpty()) {
      path = "/";
    }
    String query = urlObj.getQuery();
    if (query != null && !query.isEmpty()) {
      path = path + "?" + query;
    }

    // Connect socket to the host
    socket.connect(new InetSocketAddress(host, port));

    try {
      // Create and send HTTP GET request
      String request = "GET " + path + " HTTP/1.1\r\n";
      request += "Host: " + host + "\r\n";
      request += "Connection: close\r\n";
      request += "\r\n";

      socket.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().flush();

      // Read response
      byte[] responseBytes = readAllBytes(socket.getInputStream());
      String responseStr = new String(responseBytes, StandardCharsets.UTF_8);

      // Parse status code and body
      int statusCode = parseStatusCode(responseStr);
      byte[] body = extractBody(responseBytes);

      return new HttpResponse(statusCode, body);
    } finally {
      socket.close();
    }
  }



  /**
   * Extracts the status code from an HTTP response.
   *
   * @param response the full HTTP response
   * @return the status code
   * @throws IllegalArgumentException if status code cannot be parsed
   */
  private int parseStatusCode(String response) {
    int endOfLine = response.indexOf("\r\n");
    if (endOfLine == -1) {
      endOfLine = response.indexOf("\n");
    }

    if (endOfLine == -1) {
      throw new IllegalArgumentException("Invalid HTTP response");
    }

    String statusLine = response.substring(0, endOfLine);
    String[] parts = statusLine.split(" ");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid status line: " + statusLine);
    }

    try {
      return Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid status code: " + parts[1], e);
    }
  }

  /**
   * Extracts the body from an HTTP response (as bytes).
   *
   * @param responseBytes the full HTTP response as bytes
   * @return the body bytes
   */
  private byte[] extractBody(byte[] responseBytes) {
    // Find the end of headers (empty line: \r\n\r\n or \n\n)
    int bodyStart = -1;

    // Try \r\n\r\n first (standard HTTP)
    for (int i = 0; i < responseBytes.length - 3; i++) {
      if (responseBytes[i] == '\r' && responseBytes[i + 1] == '\n' && responseBytes[i + 2] == '\r'
          && responseBytes[i + 3] == '\n') {
        bodyStart = i + 4;
        break;
      }
    }

    // If not found, try \n\n
    if (bodyStart == -1) {
      for (int i = 0; i < responseBytes.length - 1; i++) {
        if (responseBytes[i] == '\n' && responseBytes[i + 1] == '\n') {
          bodyStart = i + 2;
          break;
        }
      }
    }

    if (bodyStart == -1) {
      // No body found
      return new byte[0];
    }

    byte[] body = new byte[responseBytes.length - bodyStart];
    System.arraycopy(responseBytes, bodyStart, body, 0, body.length);
    return body;
  }

  /**
   * Reads all bytes from an InputStream.
   *
   * @param is the input stream
   * @return the bytes read
   * @throws IOException if an I/O error occurs
   */
  private byte[] readAllBytes(InputStream is) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = is.read(buffer)) != -1) {
      result.write(buffer, 0, bytesRead);
    }
    return result.toByteArray();
  }
}
