package org.robolectric.res;

import java.util.List;

public class AttrData {
  private final String name;
  private final String format;
  private final List<Pair> pairs;

  public AttrData(String name, String format, List<Pair> pairs) {
    this.name = name;
    this.format = format;
    this.pairs = pairs;
  }

  public String getFormat() {
    return format;
  }

  public String getName() {
    return name;
  }

  public String getValueFor(String key) {
    if (pairs == null) return null;
    for (Pair pair : pairs) {
      if (pair.name.equals(key)) {
        return pair.value;
      }
    }
    return null;
  }

  @Override public String toString() {
  return "AttrData{" +
    "name='" + name + '\'' +
    ", format='" + format + '\'' +
    '}';
  }

  public static class Pair {
    private final String name;
    private final String value;

    public Pair(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }
}
