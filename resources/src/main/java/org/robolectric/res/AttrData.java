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

  public boolean isValue(String value) {
    if (pairs == null) {
      return false;
    } else {
      for (Pair pair : pairs) {
        if (pair.value.equals(value)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override public String toString() {
    StringBuilder builder = new StringBuilder("AttrData{name='")
        .append(name)
        .append("', format='")
        .append(format)
        .append('\'');
    if (pairs != null) {
      for (Pair p : pairs) {
        builder.append(' ')
            .append(p.name)
            .append("='")
            .append(p.value)
            .append('\'');
      }
    }
    builder.append('}');
    return builder.toString();
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
