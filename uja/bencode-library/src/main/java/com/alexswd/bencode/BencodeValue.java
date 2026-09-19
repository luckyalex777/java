package com.alexswd.bencode;

/**
 * Sealed abstract class representing all bencode types. Permitted subclasses: BencodeInteger,
 * BencodeString, BencodeList, BencodeDict.
 */
public sealed
abstract class BencodeValue
permits BencodeInteger, BencodeString, BencodeList, BencodeDict
{

  /**
   * Encodes this bencode value to bencode format string.
   *
   * @return the bencode encoded string
   */
  public abstract String encode();

  /**
   * Converts this bencode value to standard Java types. - BencodeInteger -> Long - BencodeString ->
   * String - BencodeList -> List with recursive conversion - BencodeDict -> Map with String keys
   * and recursive value conversion
   *
   * @return the Java representation of this value
   */
  public abstract Object toJava();

  @Override
  public abstract String toString();
}
