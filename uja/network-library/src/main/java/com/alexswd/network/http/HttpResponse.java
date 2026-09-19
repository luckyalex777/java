package com.alexswd.network.http;

import java.util.Arrays;

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
    this.body = Arrays.copyOf(body, body.length);
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
    return Arrays.copyOf(body, body.length);
  }
}
