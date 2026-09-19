package com.alexswd.httpclientdemo;

import java.nio.charset.StandardCharsets;

/**
 * Utility class for formatting website data for display.
 *
 * <p>
 * Provides static methods to format various data types used in HTTP responses: HTTP status codes,
 * response body previews, and visual separators for console output.
 */
public final class WebsiteDataFormatter {

  /** Visual separator line for console output. */
  private static final String SEPARATOR =
      "═══════════════════════════════════════════════════════════════";

  private WebsiteDataFormatter() {
    // Utility class - prevent instantiation
  }

  /**
   * Prints a visual separator line to standard output.
   *
   * <p>
   * Useful for separating sections in console output.
   */
  public static void printSeparator() {
    System.out.println(SEPARATOR);
  }

  /**
   * Prints a section header with the given title.
   *
   * @param title the title of the section
   */
  public static void printHeader(String title) {
    System.out.println("  " + title);
    System.out.println("  " + "─".repeat(title.length()));
  }

  /**
   * Formats and prints an HTTP status line.
   *
   * @param status the HTTP status code
   */
  public static void formatStatusLine(int status) {
    final String statusMessage = getStatusMessage(status);
    System.out.printf("  Status Code: %d (%s)%n", status, statusMessage);
  }

  /**
   * Formats and prints a preview of the response body.
   *
   * @param body the response body as bytes
   * @param maxLength the maximum number of characters to display
   */
  public static void formatBodyPreview(byte[] body, int maxLength) {
    if (body == null || body.length == 0) {
      System.out.println("  [empty body]");
      return;
    }

    final String bodyString = new String(body, StandardCharsets.UTF_8);
    final String preview;

    if (bodyString.length() > maxLength) {
      preview = bodyString.substring(0, maxLength) + "...";
    } else {
      preview = bodyString;
    }

    // Print with proper formatting
    for (final String line : preview.split("\n")) {
      System.out.println("  " + line);
    }
  }

  /**
   * Returns a human-readable message for the given HTTP status code.
   *
   * @param status the HTTP status code
   * @return a descriptive message for the status code
   */
  private static String getStatusMessage(int status) {
    return switch (status) {
      case 200 -> "OK";
      case 201 -> "Created";
      case 204 -> "No Content";
      case 301 -> "Moved Permanently";
      case 302 -> "Found";
      case 304 -> "Not Modified";
      case 400 -> "Bad Request";
      case 401 -> "Unauthorized";
      case 403 -> "Forbidden";
      case 404 -> "Not Found";
      case 500 -> "Internal Server Error";
      case 502 -> "Bad Gateway";
      case 503 -> "Service Unavailable";
      default -> "HTTP Status " + status;
    };
  }
}
