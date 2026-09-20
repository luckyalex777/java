package com.alexswd.torrent.parser;

/**
 * Exception thrown when there is an error parsing or processing torrent files.
 */
public class TorrentException extends Exception {

  /**
   * Constructs a new TorrentException with the specified detail message.
   *
   * @param message the detail message
   */
  public TorrentException(String message) {
    super(message);
  }

  /**
   * Constructs a new TorrentException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public TorrentException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new TorrentException with the specified cause.
   *
   * @param cause the cause of the exception
   */
  public TorrentException(Throwable cause) {
    super(cause);
  }
}
