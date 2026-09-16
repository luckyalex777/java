package com.alexswd.bencode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a bencode list value in the format: l<items>e
 */
public final class BencodeList extends BencodeValue {

  private final List<BencodeValue> values;

  /**
   * Constructs an empty BencodeList.
   */
  public BencodeList() {
    this.values = new ArrayList<>();
  }

  /**
   * Constructs a BencodeList with the specified values.
   *
   * @param values the list of bencode values
   */
  public BencodeList(List<BencodeValue> values) {
    this.values = new ArrayList<>(Objects.requireNonNull(values));
  }

  /**
   * Adds a bencode value to this list.
   *
   * @param value the bencode value to add
   */
  public void add(BencodeValue value) {
    values.add(Objects.requireNonNull(value));
  }

  /**
   * Gets the list of values.
   *
   * @return a copy of the values list
   */
  public List<BencodeValue> getValues() {
    return new ArrayList<>(values);
  }

  /**
   * Gets the size of this list.
   *
   * @return the number of elements in this list
   */
  public int size() {
    return values.size();
  }

  /**
   * Gets an element at the specified index.
   *
   * @param index the index of the element
   * @return the bencode value at the index
   */
  public BencodeValue get(int index) {
    return values.get(index);
  }

  @Override
  public String encode() {
    StringBuilder sb = new StringBuilder("l");
    for (BencodeValue value : values) {
      sb.append(value.encode());
    }
    sb.append("e");
    return sb.toString();
  }

  @Override
  public Object toJava() {
    List<Object> result = new ArrayList<>();
    for (BencodeValue value : values) {
      result.add(value.toJava());
    }
    return result;
  }

  @Override
  public String toString() {
    return "BencodeList{" + values + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BencodeList)) {
      return false;
    }
    BencodeList that = (BencodeList) o;
    return values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return values.hashCode();
  }
}
