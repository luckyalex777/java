package com.alexswd.bencode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Represents a bencode string value in the format: <length>:<string>
 */
public final class BencodeString extends BencodeValue {

  private final byte[] value;

  /**
   * Constructs a BencodeString from a String.
   *
   * @param value the string value to represent
   */
  public BencodeString(String value) {
    // Use ISO-8859-1 to preserve all byte values (0-255) when converting strings
    // This is necessary because bencode is binary-safe and may contain non-UTF-8 data
    this.value = value.getBytes(StandardCharsets.ISO_8859_1);
  }

  /**
   * Constructs a BencodeString from a byte array.
   *
   * @param value the byte array value to represent
   */
  public BencodeString(byte[] value) {
    this.value = value.clone();
  }

  /**
   * Gets the string value as UTF-8 decoded string.
   *
   * @return the string value
   */
  public String getValue() {
    // Try UTF-8 first for text content, fall back to ISO-8859-1 for binary data
    try {
      return new String(value, StandardCharsets.UTF_8);
    } catch (Exception e) {
      return new String(value, StandardCharsets.ISO_8859_1);
    }
  }

  /**
   * Gets the raw byte array value.
   *
   * @return a copy of the byte array value
   */
  public byte[] getByteValue() {
    return value.clone();
  }

  @Override
  public String encode() {
    return value.length + ":" + getValue();
  }

  @Override
  public Object toJava() {
    return getValue();
  }

  @Override
  public String toString() {
    return "BencodeString{" + getValue() + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BencodeString)) {
      return false;
    }
    BencodeString that = (BencodeString) o;
    return Arrays.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }
}
