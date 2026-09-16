package com.alexswd.bencode;

/**
 * Represents a bencode integer value in the format: i<number>e
 */
public final class BencodeInteger extends BencodeValue {

  private final long value;

  /**
   * Constructs a BencodeInteger with the specified value.
   *
   * @param value the integer value to represent
   */
  public BencodeInteger(long value) {
    this.value = value;
  }

  /**
   * Gets the integer value.
   *
   * @return the integer value
   */
  public long getValue() {
    return value;
  }

  @Override
  public String encode() {
    return "i" + value + "e";
  }

  @Override
  public Object toJava() {
    return value;
  }

  @Override
  public String toString() {
    return "BencodeInteger{" + value + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BencodeInteger)) {
      return false;
    }
    BencodeInteger that = (BencodeInteger) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(value);
  }
}
