package com.alexswd.bencode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Decoder for parsing bencode format strings into BencodeValue objects.
 */
public final class BencodeDecoder {

  /**
   * Decodes a bencode formatted string into a BencodeValue.
   *
   * @param input the bencode formatted string
   * @return the decoded BencodeValue
   * @throws BencodeException if the input is malformed
   */
  public BencodeValue decode(String input) throws BencodeException {
    Objects.requireNonNull(input);
    if (input.isEmpty()) {
      throw new BencodeException("Input cannot be empty");
    }
    Parser parser = new Parser(input);
    BencodeValue result = parser.parseValue();
    if (parser.position < input.length()) {
      throw new BencodeException("Unexpected characters after bencode value");
    }
    return result;
  }

  /**
   * Internal parser helper class.
   */
  private static final class Parser {

    private final String input;
    int position = 0;

    Parser(String input) {
      this.input = input;
    }

    BencodeValue parseValue() throws BencodeException {
      if (position >= input.length()) {
        throw new BencodeException("Unexpected end of input");
      }

      char ch = input.charAt(position);
      if (ch == 'i') {
        return parseInteger();
      } else if (ch == 'l') {
        return parseList();
      } else if (ch == 'd') {
        return parseDict();
      } else if (Character.isDigit(ch)) {
        return parseString();
      } else {
        throw new BencodeException("Invalid bencode character: " + ch + " at position " + position);
      }
    }

    BencodeInteger parseInteger() throws BencodeException {
      if (input.charAt(position) != 'i') {
        throw new BencodeException("Expected 'i' for integer at position " + position);
      }
      position++;

      StringBuilder sb = new StringBuilder();
      boolean negative = false;

      if (position < input.length() && input.charAt(position) == '-') {
        negative = true;
        position++;
      }

      if (position >= input.length() || !Character.isDigit(input.charAt(position))) {
        throw new BencodeException("Expected digit after 'i' or '-' at position " + position);
      }

      while (position < input.length() && Character.isDigit(input.charAt(position))) {
        sb.append(input.charAt(position));
        position++;
      }

      if (position >= input.length() || input.charAt(position) != 'e') {
        throw new BencodeException("Expected 'e' to terminate integer at position " + position);
      }
      position++;

      long value = Long.parseLong(sb.toString());
      if (negative) {
        value = -value;
      }
      return new BencodeInteger(value);
    }

    BencodeString parseString() throws BencodeException {
      StringBuilder lengthSb = new StringBuilder();

      while (position < input.length() && Character.isDigit(input.charAt(position))) {
        lengthSb.append(input.charAt(position));
        position++;
      }

      if (lengthSb.length() == 0) {
        throw new BencodeException("Expected string length at position " + position);
      }

      int length;
      try {
        length = Integer.parseInt(lengthSb.toString());
      } catch (NumberFormatException e) {
        throw new BencodeException("Invalid string length: " + lengthSb);
      }

      if (position >= input.length() || input.charAt(position) != ':') {
        throw new BencodeException("Expected ':' after string length at position " + position);
      }
      position++;

      if (position + length > input.length()) {
        throw new BencodeException("Not enough characters for string: expected " + length
            + " but got " + (input.length() - position));
      }

      String stringValue = input.substring(position, position + length);
      position += length;

      return new BencodeString(stringValue);
    }

    BencodeList parseList() throws BencodeException {
      if (input.charAt(position) != 'l') {
        throw new BencodeException("Expected 'l' for list at position " + position);
      }
      position++;

      List<BencodeValue> values = new ArrayList<>();

      while (position < input.length() && input.charAt(position) != 'e') {
        values.add(parseValue());
      }

      if (position >= input.length()) {
        throw new BencodeException("Expected 'e' to terminate list");
      }
      position++;

      return new BencodeList(values);
    }

    BencodeDict parseDict() throws BencodeException {
      if (input.charAt(position) != 'd') {
        throw new BencodeException("Expected 'd' for dict at position " + position);
      }
      position++;

      Map<String, BencodeValue> values = new HashMap<>();
      String lastKey = null;

      while (position < input.length() && input.charAt(position) != 'e') {
        // Keys must be strings
        if (!Character.isDigit(input.charAt(position))) {
          throw new BencodeException("Expected string key in dict at position " + position);
        }

        BencodeString keyValue = parseString();
        String key = keyValue.getValue();

        // Validate key ordering (keys must be in sorted order)
        if (lastKey != null && key.compareTo(lastKey) <= 0) {
          throw new BencodeException(
              "Dictionary keys are not sorted: '" + lastKey + "' followed by '" + key + "'");
        }
        lastKey = key;

        BencodeValue value = parseValue();
        values.put(key, value);
      }

      if (position >= input.length()) {
        throw new BencodeException("Expected 'e' to terminate dictionary");
      }
      position++;

      return new BencodeDict(values);
    }
  }
}
