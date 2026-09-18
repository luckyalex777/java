package com.alexswd.network.http;

/**
 * Represents an HTTP response.
 */
public class HttpResponse {
  private final int status;
  private final byte[] body;

  /**
   * Creates a new HTTP response.
   *
   * @param status the HTTP status code
   * @param body the response body as bytes
   */
  public HttpResponse(int status, byte[] body) {
    this.status = status;
    this.body = body;
  }

  /**
   * Gets the HTTP status code.
   *
   * @return the status code
   */
  public int getStatus() {
    return status;
  }

  /**
   * Gets the response body as bytes.
   *
   * @return the response body
   */
  public byte[] getBody() {
    return body;
  }
}
