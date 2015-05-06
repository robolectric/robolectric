package org.robolectric.res;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResBundle<T> {
  // Matches a version qualifier like "v14". Parentheses capture the numeric
  // part for easy retrieval with Matcher.group(1).
  private static final String VERSION_QUALIFIER_REGEX = "v([0-9]+)";
  private static final String PADDED_VERSION_QUALIFIER_REGEX
      = "-" + VERSION_QUALIFIER_REGEX + "-";
  private static final Pattern VERSION_QUALIFIER_PATTERN_WITH_LINE_END
      = Pattern.compile(VERSION_QUALIFIER_REGEX + "$");
  private static final Pattern VERSION_QUALIFIER_PATTERN_WITH_DASHES
      = Pattern.compile(PADDED_VERSION_QUALIFIER_REGEX);

  private final ResMap<T> valuesMap = new ResMap<>();
  private final ResMap<List<T>> valuesArrayMap = new ResMap<>();
  private String overrideNamespace;

  public void put(String attrType, String name, T value, XmlLoader.XmlContext xmlContext) {
    ResName resName = new ResName(maybeOverride(xmlContext.packageName), attrType, name);
    List<Value<T>> values = valuesMap.find(resName);
    values.add(new Value<>(xmlContext.getQualifiers(), value));
    Collections.sort(values);
  }

  public T get(ResName resName, String qualifiers) {
    Value<T> value = getValue(resName, qualifiers);
    return value == null ? null : value.value;
  }

  public Value<T> getValue(ResName resName, String qualifiers) {
    List<Value<T>> values = valuesMap.find(maybeOverride(resName));
    return values != null ? pick(values, qualifiers) : null;
  }

  public static int getVersionQualifierApiLevel(String qualifiers) {
    Matcher m = VERSION_QUALIFIER_PATTERN_WITH_LINE_END.matcher(qualifiers);
    if (m.find()) {
      return Integer.parseInt(m.group(1));
    }
    return -1;
  }

  public static <T> Value<T> pick(List<Value<T>> values, String qualifiers) {
    final int count = values.size();
    if (count == 0) return null;

    BitSet possibles = new BitSet(count);
    possibles.set(0, count);

    StringTokenizer st = new StringTokenizer(qualifiers, "-");
    while (st.hasMoreTokens()) {
      String qualifier = st.nextToken();
      String paddedQualifier = "-" + qualifier + "-";
      BitSet matches = new BitSet(count);

      for (int i = possibles.nextSetBit(0); i != -1; i = possibles.nextSetBit(i + 1)) {
        if (values.get(i).qualifiers.contains(paddedQualifier)) {
          matches.set(i);
        }
      }

      if (!matches.isEmpty()) {
        possibles.and(matches); // eliminate any that didn't match this qualifier
      }

      if (matches.cardinality() == 1) break;
    }

    /*
     * If any resources out of the possibles have version qualifiers, return the
     * closest match that doesn't go over. This is the last step because it's lowest
     * in the precedence table at:
     * https://developer.android.com/guide/topics/resources/providing-resources.html#table2
     */
    int targetApiLevel = getVersionQualifierApiLevel(qualifiers);
    if (qualifiers.length() > 0 && targetApiLevel != -1) {
      Value<T> bestMatch = null;
      int bestMatchDistance = Integer.MAX_VALUE;
      for (int i = possibles.nextSetBit(0); i != -1; i = possibles.nextSetBit(i + 1)) {
        Value<T> value = values.get(i);
        int distance = getDistance(value, targetApiLevel);
        // Remove the version part and see if they still match
        String paddedQualifier = "-" + qualifiers + "-";
        String valueWithoutVersion = VERSION_QUALIFIER_PATTERN_WITH_DASHES.matcher(value.qualifiers).replaceAll("--");
        String qualifierWithoutVersion = VERSION_QUALIFIER_PATTERN_WITH_DASHES.matcher(paddedQualifier).replaceAll("--");
        if (qualifierWithoutVersion.contains(valueWithoutVersion) && distance >= 0 && distance < bestMatchDistance) {
          bestMatch = value;
          bestMatchDistance = distance;
        }
      }
      if (bestMatch != null) {
        return bestMatch;
      }
    }

    int i = possibles.nextSetBit(0);
    if (i != -1) return values.get(i);

    throw new IllegalStateException("couldn't handle qualifiers \"" + qualifiers + "\"");
  }

  /*
   * Gets the difference between the version qualifier of val and targetApiLevel.
   *
   * Return value:
   * - Lower number is a better match (0 is a perfect match)
   * - Less than zero: val's version qualifier is greater than targetApiLevel,
   *   or val has no version qualifier
   */
  private static int getDistance(Value val, int targetApiLevel) {
    int distance = -1;
    Matcher m = VERSION_QUALIFIER_PATTERN_WITH_DASHES.matcher(val.qualifiers);
    if (m.find()) {
      String match = m.group(1);
      int resApiLevel = Integer.parseInt(match);
      distance = targetApiLevel - resApiLevel;

      if (m.find()) {
        throw new IllegalStateException("A resource file was found that had two API level qualifiers: " + val);
      }
    } else {
      if (val.qualifiers.equals("--")) {
        distance = targetApiLevel;
      }
    }
    return distance;
  }

  public int size() {
    return valuesMap.size() + valuesArrayMap.size();
  }

  public void makeImmutable() {
    valuesMap.makeImmutable();
    valuesArrayMap.makeImmutable();
  }

  public void overrideNamespace(String overrideNamespace) {
    this.overrideNamespace = overrideNamespace;
    if (size() > 0) throw new RuntimeException();
  }

  String maybeOverride(String namespace) {
    return overrideNamespace == null ? namespace : overrideNamespace;
  }

  ResName maybeOverride(ResName resName) {
    return overrideNamespace == null ? resName : new ResName(overrideNamespace, resName.type, resName.name);
  }

  public void mergeLibraryStyle(ResBundle<T> fromResBundle, String packageName) {
    valuesMap.merge(packageName, fromResBundle.valuesMap);
    valuesArrayMap.merge(packageName, fromResBundle.valuesArrayMap);
  }

  public static class Value<T> implements Comparable<Value<T>> {
    private final String qualifiers;
    private final T value;

    Value(String qualifiers, T value) {
      if (value == null) {
        throw new NullPointerException();
      }

      this.qualifiers = qualifiers == null ? "--" : "-" + qualifiers + "-";
      this.value = value;
    }

    public String getQualifiers() {
      return qualifiers;
    }

    public T getValue() {
      return value;
    }

    @Override
    public int compareTo(Value<T> o) {
      return qualifiers.compareTo(o.qualifiers);
    }

    @Override public String toString() {
      return "Value{" +
          "qualifiers='" + qualifiers + '\'' +
          ", value=" + value +
          '}';
    }
  }

  private static class ResMap<T> {
    private final Map<ResName, List<Value<T>>> map = new HashMap<>();
    private boolean immutable;

    public List<Value<T>> find(ResName resName) {
      List<Value<T>> values = map.get(resName);
      if (values == null) map.put(resName, values = new ArrayList<>());
      return values;
    }

    private void merge(String packageName, ResMap<T> sourceMap) {
      if (immutable) {
        throw new IllegalStateException("immutable!");
      }

      for (Map.Entry<ResName, List<Value<T>>> entry : sourceMap.map.entrySet()) {
        ResName resName = entry.getKey().withPackageName(packageName);
        find(resName).addAll(entry.getValue());
      }
    }

    public int size() {
      return map.size();
    }

    public void makeImmutable() {
      immutable = true;
    }
  }
}
