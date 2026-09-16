package com.alexswd.bencode;

/**
 * Custom exception for bencode parsing and encoding errors.
 */
public class BencodeException extends Exception {

  /**
   * Constructs a BencodeException with the specified detail message.
   *
   * @param message the detail message
   */
  public BencodeException(String message) {
    super(message);
  }

  /**
   * Constructs a BencodeException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause of the exception
   */
  public BencodeException(String message, Throwable cause) {
    super(message, cause);
  }
}
