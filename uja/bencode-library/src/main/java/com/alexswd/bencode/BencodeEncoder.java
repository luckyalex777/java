package com.alexswd.bencode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Encoder for converting Java objects to bencode format.
 */
public final class BencodeEncoder {

  private BencodeEncoder() {
    // Utility class - prevent instantiation
  }

  /**
   * Encodes a BencodeValue to bencode format string.
   *
   * @param value the bencode value to encode
   * @return the bencode encoded string
   */
  public static String encode(BencodeValue value) {
    Objects.requireNonNull(value);
    return value.encode();
  }

  /**
   * Converts a Java object to a BencodeValue and encodes it. Supports: Long, Integer, String, List,
   * Map, BencodeValue
   *
   * @param obj the Java object to encode
   * @return the bencode encoded string
   * @throws BencodeException if the object type is not supported
   */
  public static String encodeObject(Object obj) throws BencodeException {
    BencodeValue value = objectToBencodeValue(obj);
    return value.encode();
  }

  /**
   * Converts a Java object to a BencodeValue. Supports: Long, Integer, String, List, Map,
   * BencodeValue
   *
   * @param obj the Java object to convert
   * @return the corresponding BencodeValue
   * @throws BencodeException if the object type is not supported
   */
  public static BencodeValue objectToBencodeValue(Object obj) throws BencodeException {
    Objects.requireNonNull(obj);

    if (obj instanceof BencodeValue) {
      return (BencodeValue) obj;
    } else if (obj instanceof Long) {
      return new BencodeInteger((Long) obj);
    } else if (obj instanceof Integer) {
      return new BencodeInteger(((Integer) obj).longValue());
    } else if (obj instanceof String) {
      return new BencodeString((String) obj);
    } else if (obj instanceof List<?>) {
      return listToBencodeList((List<?>) obj);
    } else if (obj instanceof Map<?, ?>) {
      return mapToBencodeDict((Map<?, ?>) obj);
    } else {
      throw new BencodeException("Unsupported object type: " + obj.getClass().getName());
    }
  }

  private static BencodeList listToBencodeList(List<?> list) throws BencodeException {
    BencodeList bencodeList = new BencodeList();
    for (Object item : list) {
      bencodeList.add(objectToBencodeValue(item));
    }
    return bencodeList;
  }

  private static BencodeDict mapToBencodeDict(Map<?, ?> map) throws BencodeException {
    BencodeDict bencodeDict = new BencodeDict();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String key = String.valueOf(entry.getKey());
      bencodeDict.put(key, objectToBencodeValue(entry.getValue()));
    }
    return bencodeDict;
  }
}
