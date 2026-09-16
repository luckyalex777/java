package com.alexswd.bencode;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Represents a bencode dictionary value in the format: d<key><value>...e Dictionary keys are sorted
 * alphabetically in the encoded format.
 */
public final class BencodeDict extends BencodeValue {

  private final Map<String, BencodeValue> values;

  /**
   * Constructs an empty BencodeDict.
   */
  public BencodeDict() {
    this.values = new HashMap<>();
  }

  /**
   * Constructs a BencodeDict with the specified values.
   *
   * @param values the map of string keys to bencode values
   */
  public BencodeDict(Map<String, BencodeValue> values) {
    this.values = new HashMap<>(Objects.requireNonNull(values));
  }

  /**
   * Puts a key-value pair into this dictionary.
   *
   * @param key the string key
   * @param value the bencode value
   */
  public void put(String key, BencodeValue value) {
    values.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
  }

  /**
   * Gets the value associated with the specified key.
   *
   * @param key the string key
   * @return the bencode value, or null if not found
   */
  public BencodeValue get(String key) {
    return values.get(key);
  }

  /**
   * Gets all key-value pairs.
   *
   * @return a copy of the map
   */
  public Map<String, BencodeValue> getValues() {
    return new HashMap<>(values);
  }

  /**
   * Gets the size of this dictionary.
   *
   * @return the number of key-value pairs
   */
  public int size() {
    return values.size();
  }

  @Override
  public String encode() {
    StringBuilder sb = new StringBuilder("d");
    // Sort keys alphabetically for bencode format compliance
    Map<String, BencodeValue> sorted = new TreeMap<>(values);
    for (Map.Entry<String, BencodeValue> entry : sorted.entrySet()) {
      // Encode the key as a bencode string
      BencodeString keyString = new BencodeString(entry.getKey());
      sb.append(keyString.encode());
      // Encode the value
      sb.append(entry.getValue().encode());
    }
    sb.append("e");
    return sb.toString();
  }

  @Override
  public Object toJava() {
    Map<String, Object> result = new LinkedHashMap<>();
    for (Map.Entry<String, BencodeValue> entry : values.entrySet()) {
      result.put(entry.getKey(), entry.getValue().toJava());
    }
    return result;
  }

  @Override
  public String toString() {
    return "BencodeDict{" + values + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BencodeDict)) {
      return false;
    }
    BencodeDict that = (BencodeDict) o;
    return values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }
}
