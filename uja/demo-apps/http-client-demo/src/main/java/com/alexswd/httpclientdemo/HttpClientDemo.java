package com.alexswd.httpclientdemo;

import com.alexswd.network.http.HttpClient;
import com.alexswd.network.http.HttpResponse;
import com.alexswd.network.tcp.ISocket;
import com.alexswd.network.tcp.PlainSocket;
import com.alexswd.network.tcp.TorSocket;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console application for making HTTP requests and displaying responses.
 *
 * <p>
 * This application connects to a website identified via command-line argument, retrieves the HTTP
 * response, and displays the status code, response size, and a preview of the response body.
 *
 * <p>
 * Usage: java -jar http-client-demo.jar &lt;url&gt;
 */
public final class HttpClientDemo {

  private static final Logger logger = LoggerFactory.getLogger(HttpClientDemo.class);

  /** Maximum number of characters to display from response body. */
  private static final int MAX_PREVIEW_LENGTH = 200;

  private HttpClientDemo() {
    // Utility class - prevent instantiation
  }

  /**
   * Main entry point for the HTTP client demo application.
   *
   * @param args command line arguments; first argument should be the URL to request
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      System.exit(1);
    }

    try {
      final String url = args[0];

      // Validate URL format
      validateUrl(url);

      logger.info("Fetching URL: {}", url);

      testClient(url, false);
      testClient(url, true);

    } catch (MalformedURLException e) {
      System.err.println("Error: Invalid URL format");
      System.err.println("Reason: " + e.getMessage());
      logger.error("Malformed URL", e);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Error: Failed to fetch URL");
      System.err.println("Reason: " + e.getMessage());
      logger.error("IOException while fetching URL", e);
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Error: Unexpected error occurred");
      System.err.println("Reason: " + e.getMessage());
      logger.error("Unexpected exception", e);
      System.exit(1);
    }
  }

  private static void testClient(String url, boolean useTor) throws IOException {
    ISocket socket = useTor ? new TorSocket() : new PlainSocket();

    // Create HTTP client and make request
    final HttpClient httpClient = new HttpClient(socket);
    var headers = new HashMap();
    headers.put("User-Agent", "uja");
    final HttpResponse response = httpClient.get(url, headers);

    // Display response information
    displayResponse(response);
    logger.info("Successfully fetched URL");
  }

  /**
   * Prints usage information to standard output.
   */
  private static void printUsage() {
    System.out.println("HTTP Client Demo");
    System.out.println("Usage: java -jar http-client-demo.jar <url>");
    System.out.println();
    System.out.println("Example: java -jar http-client-demo.jar http://example.com");
  }

  /**
   * Validates that the provided URL string is well-formed.
   *
   * @param url the URL string to validate
   * @throws MalformedURLException if the URL is malformed
   */
  private static void validateUrl(String url) throws MalformedURLException {
    new java.net.URL(url);
  }

  /**
   * Displays the HTTP response in a formatted manner.
   *
   * @param response the HTTP response to display
   */
  private static void displayResponse(HttpResponse response) {
    final int status = response.getStatus();
    final byte[] body = response.getBody();

    WebsiteDataFormatter.printSeparator();
    System.out.println("HTTP RESPONSE");
    WebsiteDataFormatter.printSeparator();

    // Status information
    WebsiteDataFormatter.printHeader("STATUS");
    WebsiteDataFormatter.formatStatusLine(status);

    // Response size information
    WebsiteDataFormatter.printHeader("RESPONSE BODY");
    System.out.printf("  Body Size: %d bytes%n", body.length);

    // Body preview
    if (body.length > 0) {
      WebsiteDataFormatter.printHeader("BODY PREVIEW");
      WebsiteDataFormatter.formatBodyPreview(body, MAX_PREVIEW_LENGTH);
    }

    WebsiteDataFormatter.printSeparator();
  }
}
