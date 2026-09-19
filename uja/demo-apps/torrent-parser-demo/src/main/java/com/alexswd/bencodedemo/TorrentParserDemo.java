package com.alexswd.bencodedemo;

import com.alexswd.torrent.TorrentInfo;
import com.alexswd.torrent.TorrentParser;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Console application for parsing and displaying torrent file information.
 *
 * <p>
 * This application reads a torrent file and displays its metadata including name, total size, piece
 * size, number of pieces, info hash, announce URLs, file list, creation date, comment, and creator
 * information.
 *
 * <p>
 * Usage: java -jar torrent-parser-demo.jar &lt;path/to/torrent/file.torrent&gt;
 */
public final class TorrentParserDemo {

  private static final Logger logger = LoggerFactory.getLogger(TorrentParserDemo.class);

  /** Maximum number of items to display before indicating there are more. */
  private static final int MAX_DISPLAY_ITEMS = 5;

  private TorrentParserDemo() {
    // Utility class - prevent instantiation
  }

  /**
   * Main entry point for the torrent parser demo application.
   *
   * @param args command line arguments; first argument should be path to torrent file
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      System.exit(1);
    }

    try {
      String filePath = args[0];
      File torrentFile = new File(filePath);

      // Validate file exists
      if (!torrentFile.exists()) {
        System.err.println("Error: File not found: " + filePath);
        System.exit(1);
      }

      // Validate file is readable
      if (!torrentFile.canRead()) {
        System.err.println("Error: File is not readable: " + filePath);
        System.exit(1);
      }

      // Parse torrent file
      logger.info("Parsing torrent file: {}", filePath);
      TorrentInfo torrentInfo = TorrentParser.parse(torrentFile);

      // Display torrent information
      displayTorrentInfo(torrentInfo);
      logger.info("Successfully parsed torrent file");

    } catch (Exception e) {
      System.err.println("Error: Failed to parse torrent file");
      System.err.println("Reason: " + e.getMessage());
      logger.error("Exception while parsing torrent", e);
      System.exit(1);
    }
  }

  /**
   * Prints usage information to standard output.
   */
  private static void printUsage() {
    System.out.println("Torrent Parser Demo");
    System.out.println("Usage: java -jar torrent-parser-demo.jar <path/to/torrent/file.torrent>");
  }

  /**
   * Displays all torrent information in a formatted manner.
   *
   * @param torrentInfo the torrent information to display
   */
  private static void displayTorrentInfo(TorrentInfo torrentInfo) {
    TorrentDataFormatter.printSeparator();
    System.out.println("TORRENT INFORMATION");
    TorrentDataFormatter.printSeparator();

    // Basic information
    TorrentDataFormatter.printHeader("BASIC INFORMATION");
    displayBasicInfo(torrentInfo);

    // Files information
    TorrentDataFormatter.printHeader("FILES");
    displayFilesInfo(torrentInfo);

    // Announce URLs
    TorrentDataFormatter.printHeader("ANNOUNCE URLS");
    displayAnnounceUrls(torrentInfo);

    // Optional information
    TorrentDataFormatter.printHeader("ADDITIONAL INFORMATION");
    displayOptionalInfo(torrentInfo);

    TorrentDataFormatter.printSeparator();
  }

  /**
   * Displays basic torrent information.
   *
   * @param torrentInfo the torrent information
   */
  private static void displayBasicInfo(TorrentInfo torrentInfo) {
    System.out.printf("  Name:                 %s%n", getName(torrentInfo));
    System.out.printf("  Total Size:           %s%n", formatSize(torrentInfo.getLength()));
    System.out.printf("  Piece Size:           %s%n", formatSize(torrentInfo.getPieceLength()));
    System.out.printf("  Number of Pieces:     %d%n", getNumberOfPieces(torrentInfo));
    System.out.printf("  Info Hash (SHA-1):    %s%n", getInfoHashHex(torrentInfo));
  }

  /**
   * Displays file information for the torrent.
   *
   * @param torrentInfo the torrent information
   */
  private static void displayFilesInfo(TorrentInfo torrentInfo) {
    List<TorrentInfo.FileEntry> files = torrentInfo.getFiles();

    if (files == null || files.isEmpty()) {
      System.out.println("  Single file torrent: " + getName(torrentInfo));
      System.out.printf("    Size: %s%n", formatSize(torrentInfo.getLength()));
      return;
    }

    System.out.printf("  Total files: %d%n", files.size());
    if (files.size() > MAX_DISPLAY_ITEMS) {
      System.out.printf("  Showing first %d files:%n", MAX_DISPLAY_ITEMS);
    }

    int displayCount = Math.min(MAX_DISPLAY_ITEMS, files.size());
    for (int i = 0; i < displayCount; i++) {
      TorrentInfo.FileEntry file = files.get(i);
      String path = String.join(File.separator, file.getPath());
      System.out.printf("    %d. %s (%s)%n", i + 1, path, formatSize(file.getLength()));
    }

    if (files.size() > MAX_DISPLAY_ITEMS) {
      System.out.printf("  ... and %d more file(s)%n", files.size() - MAX_DISPLAY_ITEMS);
    }
  }

  /**
   * Displays announce URLs from the torrent.
   *
   * @param torrentInfo the torrent information
   */
  private static void displayAnnounceUrls(TorrentInfo torrentInfo) {
    List<List<String>> announceList = torrentInfo.getAnnounceList();
    String announceUrl = torrentInfo.getAnnounceUrl();

    int totalUrls = 0;
    if (announceList != null) {
      for (List<String> tier : announceList) {
        totalUrls += tier.size();
      }
    } else if (announceUrl != null && !announceUrl.isEmpty()) {
      totalUrls = 1;
    }

    if (totalUrls == 0) {
      System.out.println("  No announce URLs found");
      return;
    }

    System.out.printf("  Total announce URLs: %d%n", totalUrls);
    if (totalUrls > MAX_DISPLAY_ITEMS) {
      System.out.printf("  Showing first %d URLs:%n", MAX_DISPLAY_ITEMS);
    }

    int displayCount = 0;
    if (announceList != null) {
      for (List<String> tier : announceList) {
        for (String url : tier) {
          displayCount++;
          System.out.printf("    %d. %s%n", displayCount, url);
          if (displayCount >= MAX_DISPLAY_ITEMS) {
            break;
          }
        }
        if (displayCount >= MAX_DISPLAY_ITEMS) {
          break;
        }
      }
    } else if (announceUrl != null && !announceUrl.isEmpty()) {
      System.out.printf("    1. %s%n", announceUrl);
      displayCount = 1;
    }

    if (totalUrls > MAX_DISPLAY_ITEMS) {
      System.out.printf("  ... and %d more URL(s)%n", totalUrls - MAX_DISPLAY_ITEMS);
    }
  }

  /**
   * Displays optional torrent information (creation date, comment, creator).
   *
   * @param torrentInfo the torrent information
   */
  private static void displayOptionalInfo(TorrentInfo torrentInfo) {
    java.util.Date creationDate = torrentInfo.getCreationDate();
    if (creationDate != null) {
      System.out.printf("  Created:              %s%n",
          TorrentDataFormatter.formatDate(creationDate));
    } else {
      System.out.println("  Created:              N/A");
    }

    String comment = torrentInfo.getComment();
    if (comment != null && !comment.isEmpty()) {
      System.out.printf("  Comment:              %s%n", comment);
    } else {
      System.out.println("  Comment:              N/A");
    }

    String createdBy = torrentInfo.getCreatedBy();
    if (createdBy != null && !createdBy.isEmpty()) {
      System.out.printf("  Created By:           %s%n", createdBy);
    } else {
      System.out.println("  Created By:           N/A");
    }
  }

  /**
   * Gets the name of the torrent.
   *
   * @param torrentInfo the torrent information
   * @return the torrent name
   */
  private static String getName(TorrentInfo torrentInfo) {
    String name = torrentInfo.getName();
    return name != null && !name.isEmpty() ? name : "Unknown";
  }

  /**
   * Gets the info hash as a hexadecimal string.
   *
   * @param torrentInfo the torrent information
   * @return the hex-encoded info hash
   */
  private static String getInfoHashHex(TorrentInfo torrentInfo) {
    byte[] infoHash = torrentInfo.getInfoHash();
    return infoHash != null ? TorrentDataFormatter.bytesToHex(infoHash) : "N/A";
  }

  /**
   * Gets the number of pieces in the torrent.
   *
   * @param torrentInfo the torrent information
   * @return the number of pieces
   */
  private static long getNumberOfPieces(TorrentInfo torrentInfo) {
    long totalLength = torrentInfo.getLength();
    long pieceLength = torrentInfo.getPieceLength();

    if (pieceLength <= 0) {
      return 0;
    }

    return (totalLength + pieceLength - 1) / pieceLength;
  }

  /**
   * Formats a size in bytes to human-readable format.
   *
   * @param bytes the size in bytes
   * @return the formatted size string
   */
  private static String formatSize(long bytes) {
    return TorrentDataFormatter.formatBytes(bytes);
  }
}
