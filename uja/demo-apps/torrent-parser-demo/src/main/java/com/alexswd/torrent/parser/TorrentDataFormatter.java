package com.alexswd.torrent.parser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utility class for formatting torrent data for display.
 *
 * <p>
 * Provides static methods to format various data types used in torrent files: byte sizes to
 * human-readable format, byte arrays to hexadecimal strings, dates to formatted strings, and visual
 * separators for console output.
 */
public final class TorrentDataFormatter {

  /** Visual separator line for console output. */
  private static final String SEPARATOR =
      "═══════════════════════════════════════════════════════════════";

  /** Buffer size for hex string building. */
  private static final int HEX_BUFFER_SIZE = 32;

  private TorrentDataFormatter() {
    // Utility class - prevent instantiation
  }

  /**
   * Converts bytes to a human-readable format.
   *
   * @param bytes the number of bytes to convert
   * @return a formatted string representation (e.g., "1.5 MB", "2.0 GB")
   */
  public static String formatBytes(long bytes) {
    if (bytes <= 0) {
      return "0 B";
    }

    final int unit = 1024;
    final String[] units = {"B", "KB", "MB", "GB", "TB"};
    int unitIndex = 0;
    double size = bytes;

    while (size >= unit && unitIndex < units.length - 1) {
      size /= unit;
      unitIndex++;
    }

    return String.format("%.2f %s", size, units[unitIndex]);
  }

  /**
   * Converts a byte array to its hexadecimal string representation.
   *
   * @param bytes the byte array to convert
   * @return the hexadecimal representation of the bytes
   */
  public static String bytesToHex(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return "";
    }

    StringBuilder hex = new StringBuilder(HEX_BUFFER_SIZE);
    for (byte b : bytes) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }

  /**
   * Formats a Date object into a readable string.
   *
   * @param date the date to format, may be null
   * @return a formatted date string, or "N/A" if date is null
   */
  public static String formatDate(Date date) {
    if (date == null) {
      return "N/A";
    }

    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    return formatter.format(Instant.ofEpochMilli(date.getTime()));
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
    System.out.println("\n" + title);
    System.out.println("───────────────────────────────────────────────────────────────");
  }
}
