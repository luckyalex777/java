package com.alexswd.bencode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BencodeDecoder and bencode library.
 */
public class BencodeDecoderTest {

  private final BencodeDecoder decoder = new BencodeDecoder();

  // ============ INTEGER TESTS ============

  /**
   * Test encoding/decoding of positive integer.
   */
  @Test
  public void testIntegerPositive() throws BencodeException {
    BencodeInteger original = new BencodeInteger(42);
    String encoded = original.encode();
    assertThat(encoded, equalTo("i42e"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeInteger.class));
    assertThat(((BencodeInteger) decoded).getValue(), equalTo(42L));
  }

  /**
   * Test encoding/decoding of negative integer.
   */
  @Test
  public void testIntegerNegative() throws BencodeException {
    BencodeInteger original = new BencodeInteger(-123);
    String encoded = original.encode();
    assertThat(encoded, equalTo("i-123e"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeInteger.class));
    assertThat(((BencodeInteger) decoded).getValue(), equalTo(-123L));
  }

  /**
   * Test encoding/decoding of zero integer.
   */
  @Test
  public void testIntegerZero() throws BencodeException {
    BencodeInteger original = new BencodeInteger(0);
    String encoded = original.encode();
    assertThat(encoded, equalTo("i0e"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeInteger.class));
    assertThat(((BencodeInteger) decoded).getValue(), equalTo(0L));
  }

  /**
   * Test encoding/decoding of large integer.
   */
  @Test
  public void testIntegerLarge() throws BencodeException {
    BencodeInteger original = new BencodeInteger(9223372036854775807L);
    String encoded = original.encode();

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeInteger.class));
    assertThat(((BencodeInteger) decoded).getValue(), equalTo(9223372036854775807L));
  }

  // ============ STRING TESTS ============

  /**
   * Test encoding/decoding of normal string.
   */
  @Test
  public void testStringNormal() throws BencodeException {
    BencodeString original = new BencodeString("hello");
    String encoded = original.encode();
    assertThat(encoded, equalTo("5:hello"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeString.class));
    assertThat(((BencodeString) decoded).getValue(), equalTo("hello"));
  }

  /**
   * Test encoding/decoding of empty string.
   */
  @Test
  public void testStringEmpty() throws BencodeException {
    BencodeString original = new BencodeString("");
    String encoded = original.encode();
    assertThat(encoded, equalTo("0:"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeString.class));
    assertThat(((BencodeString) decoded).getValue(), equalTo(""));
  }

  /**
   * Test encoding/decoding of string with special characters.
   */
  @Test
  public void testStringSpecialChars() throws BencodeException {
    BencodeString original = new BencodeString("hello:world!");
    String encoded = original.encode();
    assertThat(encoded, equalTo("12:hello:world!"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeString.class));
    assertThat(((BencodeString) decoded).getValue(), equalTo("hello:world!"));
  }

  /**
   * Test encoding/decoding of string with spaces.
   */
  @Test
  public void testStringWithSpaces() throws BencodeException {
    BencodeString original = new BencodeString("hello world");
    String encoded = original.encode();
    assertThat(encoded, equalTo("11:hello world"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeString.class));
    assertThat(((BencodeString) decoded).getValue(), equalTo("hello world"));
  }

  // ============ LIST TESTS ============

  /**
   * Test encoding/decoding of empty list.
   */
  @Test
  public void testListEmpty() throws BencodeException {
    BencodeList original = new BencodeList();
    String encoded = original.encode();
    assertThat(encoded, equalTo("le"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeList.class));
    BencodeList decodedList = (BencodeList) decoded;
    assertThat(decodedList.size(), equalTo(0));
  }

  /**
   * Test encoding/decoding of simple list with integers.
   */
  @Test
  public void testListSimple() throws BencodeException {
    BencodeList original = new BencodeList();
    original.add(new BencodeInteger(1));
    original.add(new BencodeInteger(2));
    original.add(new BencodeInteger(3));
    String encoded = original.encode();
    assertThat(encoded, equalTo("li1ei2ei3ee"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeList.class));
    BencodeList decodedList = (BencodeList) decoded;
    assertThat(decodedList.size(), equalTo(3));
    assertThat(((BencodeInteger) decodedList.get(0)).getValue(), equalTo(1L));
    assertThat(((BencodeInteger) decodedList.get(1)).getValue(), equalTo(2L));
    assertThat(((BencodeInteger) decodedList.get(2)).getValue(), equalTo(3L));
  }

  /**
   * Test encoding/decoding of list with mixed types.
   */
  @Test
  public void testListMixed() throws BencodeException {
    BencodeList original = new BencodeList();
    original.add(new BencodeInteger(42));
    original.add(new BencodeString("hello"));
    String encoded = original.encode();
    assertThat(encoded, equalTo("li42e5:helloe"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeList.class));
    BencodeList decodedList = (BencodeList) decoded;
    assertThat(decodedList.size(), equalTo(2));
    assertThat(((BencodeInteger) decodedList.get(0)).getValue(), equalTo(42L));
    assertThat(((BencodeString) decodedList.get(1)).getValue(), equalTo("hello"));
  }

  /**
   * Test encoding/decoding of nested list.
   */
  @Test
  public void testListNested() throws BencodeException {
    BencodeList inner = new BencodeList();
    inner.add(new BencodeInteger(1));
    inner.add(new BencodeInteger(2));

    BencodeList original = new BencodeList();
    original.add(new BencodeInteger(0));
    original.add(inner);
    String encoded = original.encode();
    assertThat(encoded, equalTo("li0eli1ei2eee"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeList.class));
    BencodeList decodedList = (BencodeList) decoded;
    assertThat(decodedList.size(), equalTo(2));
    assertThat(((BencodeInteger) decodedList.get(0)).getValue(), equalTo(0L));
    assertThat(decodedList.get(1), instanceOf(BencodeList.class));
  }

  // ============ DICTIONARY TESTS ============

  /**
   * Test encoding/decoding of empty dictionary.
   */
  @Test
  public void testDictEmpty() throws BencodeException {
    BencodeDict original = new BencodeDict();
    String encoded = original.encode();
    assertThat(encoded, equalTo("de"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeDict.class));
    BencodeDict decodedDict = (BencodeDict) decoded;
    assertThat(decodedDict.size(), equalTo(0));
  }

  /**
   * Test encoding/decoding of simple dictionary.
   */
  @Test
  public void testDictSimple() throws BencodeException {
    BencodeDict original = new BencodeDict();
    original.put("name", new BencodeString("John"));
    original.put("age", new BencodeInteger(30));
    String encoded = original.encode();
    // Keys are sorted: age < name
    assertThat(encoded, equalTo("d3:agei30e4:name4:Johne"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeDict.class));
    BencodeDict decodedDict = (BencodeDict) decoded;
    assertThat(decodedDict.size(), equalTo(2));
    assertThat(((BencodeInteger) decodedDict.get("age")).getValue(), equalTo(30L));
    assertThat(((BencodeString) decodedDict.get("name")).getValue(), equalTo("John"));
  }

  /**
   * Test encoding/decoding ensures dictionary keys are sorted.
   */
  @Test
  public void testDictKeySorting() throws BencodeException {
    BencodeDict original = new BencodeDict();
    original.put("z", new BencodeInteger(1));
    original.put("a", new BencodeInteger(2));
    original.put("m", new BencodeInteger(3));
    String encoded = original.encode();

    // Keys must be sorted alphabetically
    assertThat(encoded, equalTo("d1:ai2e1:mi3e1:zi1ee"));

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeDict.class));
  }

  /**
   * Test encoding/decoding of nested dictionary.
   */
  @Test
  public void testDictNested() throws BencodeException {
    BencodeDict inner = new BencodeDict();
    inner.put("city", new BencodeString("NYC"));

    BencodeDict original = new BencodeDict();
    original.put("address", inner);
    original.put("name", new BencodeString("John"));
    String encoded = original.encode();

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeDict.class));
    BencodeDict decodedDict = (BencodeDict) decoded;
    assertThat(decodedDict.get("address"), instanceOf(BencodeDict.class));
  }

  /**
   * Test encoding/decoding of complex nested structure.
   */
  @Test
  public void testDictComplexNested() throws BencodeException {
    BencodeList hobbies = new BencodeList();
    hobbies.add(new BencodeString("reading"));
    hobbies.add(new BencodeString("coding"));

    BencodeDict original = new BencodeDict();
    original.put("hobbies", hobbies);
    original.put("name", new BencodeString("John"));
    String encoded = original.encode();

    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeDict.class));
    BencodeDict decodedDict = (BencodeDict) decoded;
    assertThat(decodedDict.get("hobbies"), instanceOf(BencodeList.class));
  }

  // ============ ROUND-TRIP TESTS ============

  /**
   * Test round-trip encoding/decoding of complex structure.
   */
  @Test
  public void testRoundTripComplex() throws BencodeException {
    // Create a complex structure
    BencodeList fileList = new BencodeList();
    fileList.add(new BencodeString("file1.txt"));
    fileList.add(new BencodeString("file2.txt"));

    BencodeDict torrent = new BencodeDict();
    torrent.put("files", fileList);
    torrent.put("name", new BencodeString("test.torrent"));
    torrent.put("pieces", new BencodeInteger(12345));

    // Encode
    String encoded = torrent.encode();
    assertThat(encoded, notNullValue());

    // Decode
    BencodeValue decoded = decoder.decode(encoded);
    assertThat(decoded, instanceOf(BencodeDict.class));

    // Verify structure
    BencodeDict decodedDict = (BencodeDict) decoded;
    assertThat(decodedDict.size(), equalTo(3));
    assertThat(decodedDict.get("name"), instanceOf(BencodeString.class));
    assertThat(decodedDict.get("files"), instanceOf(BencodeList.class));
    assertThat(decodedDict.get("pieces"), instanceOf(BencodeInteger.class));

    // Re-encode and verify consistency
    String reencoded = decoded.encode();
    assertThat(reencoded, equalTo(encoded));
  }

  // ============ ERROR/EXCEPTION TESTS ============

  /**
   * Test that malformed integer throws BencodeException.
   */
  @Test
  public void testExceptionMalformedInteger() {
    assertThrows(BencodeException.class, () -> decoder.decode("i123"));
    assertThrows(BencodeException.class, () -> decoder.decode("ie"));
    assertThrows(BencodeException.class, () -> decoder.decode("i123x"));
  }

  /**
   * Test that malformed string throws BencodeException.
   */
  @Test
  public void testExceptionMalformedString() {
    assertThrows(BencodeException.class, () -> decoder.decode("5:hi"));
    assertThrows(BencodeException.class, () -> decoder.decode("5"));
    assertThrows(BencodeException.class, () -> decoder.decode("abc:hello"));
  }

  /**
   * Test that malformed list throws BencodeException.
   */
  @Test
  public void testExceptionMalformedList() {
    assertThrows(BencodeException.class, () -> decoder.decode("li1ei2e"));
    assertThrows(BencodeException.class, () -> decoder.decode("l"));
  }

  /**
   * Test that malformed dictionary throws BencodeException.
   */
  @Test
  public void testExceptionMalformedDict() {
    assertThrows(BencodeException.class, () -> decoder.decode("d1:a"));
    assertThrows(BencodeException.class, () -> decoder.decode("d"));
  }

  /**
   * Test that empty input throws BencodeException.
   */
  @Test
  public void testExceptionEmptyInput() {
    assertThrows(BencodeException.class, () -> decoder.decode(""));
  }

  /**
   * Test that invalid starting character throws BencodeException.
   */
  @Test
  public void testExceptionInvalidCharacter() {
    assertThrows(BencodeException.class, () -> decoder.decode("x123"));
    assertThrows(BencodeException.class, () -> decoder.decode("@hello"));
  }

  /**
   * Test that unsorted dictionary keys throw BencodeException.
   */
  @Test
  public void testExceptionUnsortedDictKeys() {
    // z comes before a, which violates bencode spec
    assertThrows(BencodeException.class, () -> decoder.decode("d1:zi1e1:ai2ee"));
  }

  // ============ TO_JAVA CONVERSION TESTS ============

  /**
   * Test toJava() conversion for all types.
   */
  @Test
  public void testToJavaInteger() {
    BencodeInteger value = new BencodeInteger(42);
    Object java = value.toJava();
    assertThat(java, instanceOf(Long.class));
    assertThat((Long) java, equalTo(42L));
  }

  /**
   * Test toJava() conversion for string.
   */
  @Test
  public void testToJavaString() {
    BencodeString value = new BencodeString("hello");
    Object java = value.toJava();
    assertThat(java, instanceOf(String.class));
    assertThat((String) java, equalTo("hello"));
  }

  /**
   * Test toJava() conversion for list.
   */
  @Test
  public void testToJavaList() {
    BencodeList value = new BencodeList();
    value.add(new BencodeInteger(1));
    value.add(new BencodeString("hello"));
    Object java = value.toJava();
    assertThat(java, instanceOf(List.class));
    List<?> list = (List<?>) java;
    assertThat(list.size(), equalTo(2));
    assertThat(list.get(0), equalTo(1L));
    assertThat(list.get(1), equalTo("hello"));
  }

  /**
   * Test toJava() conversion for dictionary.
   */
  @Test
  public void testToJavaDict() {
    BencodeDict value = new BencodeDict();
    value.put("name", new BencodeString("John"));
    value.put("age", new BencodeInteger(30));
    Object java = value.toJava();
    assertThat(java, instanceOf(Map.class));
    Map<?, ?> map = (Map<?, ?>) java;
    assertThat(map.size(), equalTo(2));
    assertThat(map.get("name"), equalTo("John"));
    assertThat(map.get("age"), equalTo(30L));
  }
}
