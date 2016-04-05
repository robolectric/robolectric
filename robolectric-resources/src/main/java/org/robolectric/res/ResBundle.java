package org.robolectric.res;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResBundle<T> {
  // Matches a version qualifier like "v14". Parentheses capture the numeric
  // part for easy retrieval with Matcher.group(2).
  private static final Pattern VERSION_QUALIFIER_PATTERN = Pattern.compile("(v)([0-9]+)$");
  private static final Pattern SIZE_QUALIFIER_PATTERN = Pattern.compile("(s?[wh])([0-9]+)dp$");

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
    Matcher m = VERSION_QUALIFIER_PATTERN.matcher(qualifiers);
    if (m.find()) {
      return Integer.parseInt(m.group(2));
    }
    return -1;
  }

  public static <T> Value<T> pick(List<Value<T>> values, String qualifiersStr) {
    final int count = values.size();
    if (count == 0) return null;

    Qualifiers toMatch = Qualifiers.parse(qualifiersStr);

    Qualifiers bestMatchQualifiers = null;
    Value<T> bestMatch = null;

    for (int i = 0; i < count; i++) {
      Value<T> value = values.get(i);
      Qualifiers qualifiers = Qualifiers.parse(value.qualifiers);
      if (qualifiers.matches(toMatch)) {
        if (bestMatchQualifiers == null || qualifiers.isBetterThan(bestMatchQualifiers, toMatch)) {
          bestMatchQualifiers = qualifiers;
          bestMatch =  value;
        }
      }
    }
    if (bestMatch != null) {
      return bestMatch;
    } else {
      return values.get(0);
    }
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

  private static class Qualifiers {
    // Version are matched in the end, and hence have least order
    private static final int ORDER_VERSION = 0;
    // Various size qualifies, in increasing order of importance.
    private static final List<String> INT_QUALIFIERS = Arrays.asList("v", "h", "w", "sh", "sw");
    private static final int TOTAL_ORDER_COUNT = INT_QUALIFIERS.size();

    private static Map<String, Qualifiers> sQualifiersCache = new HashMap<>();

    private final int[] mWeights = new int[TOTAL_ORDER_COUNT];
    // Set of all the qualifiers which need exact matching.
    private final List<String> mDefaults = new ArrayList<>();

    public boolean matches(Qualifiers other) {
      for (int i = 0; i < TOTAL_ORDER_COUNT; i++) {
        if (other.mWeights[i] != -1 && mWeights[i] != -1 && other.mWeights[i] < mWeights[i]) {
          return false;
        }
      }
      return other.mDefaults.containsAll(mDefaults);
    }

    public boolean isBetterThan(Qualifiers other, Qualifiers context) {
      // Compare the defaults in the order they appear in the context.
      for (String qualifier : context.mDefaults) {
        if (other.mDefaults.contains(qualifier) ^ mDefaults.contains(qualifier)) {
          return mDefaults.contains(qualifier);
        }
      }

      for (int i = TOTAL_ORDER_COUNT -1 ; i > ORDER_VERSION; i--) {
        if (other.mWeights[i] != mWeights[i]) {
          return mWeights[i] > other.mWeights[i];
        }
      }

      // Compare the version only if the context defines a version.
      if (context.mWeights[ORDER_VERSION] != -1
          && other.mWeights[ORDER_VERSION] != mWeights[ORDER_VERSION]) {
        return mWeights[ORDER_VERSION] > other.mWeights[ORDER_VERSION];
      }

      // The qualifiers match completely
      return false;
    }

    public static Qualifiers parse(String qualifiersStr) {
      synchronized (sQualifiersCache) {
        Qualifiers result = sQualifiersCache.get(qualifiersStr);
        if (result != null) {
          return result;
        }
        StringTokenizer st = new StringTokenizer(qualifiersStr, "-");
        result = new Qualifiers();
        // Version qualifiers are also allowed to match when only one of the qualifiers
        // defines a version restriction.
        result.mWeights[ORDER_VERSION] = -1;

        while (st.hasMoreTokens()) {
          String qualifier = st.nextToken();
          if (qualifier.isEmpty()) {
            continue;
          }

          Matcher m = VERSION_QUALIFIER_PATTERN.matcher(qualifier);
          if (!m.find()) {
            m = SIZE_QUALIFIER_PATTERN.matcher(qualifier);
            if (!m.find()) {
              m = null;
            }
          }
          if (m != null) {
            int order = INT_QUALIFIERS.indexOf(m.group(1));
            if (order == ORDER_VERSION && result.mWeights[ORDER_VERSION] != -1) {
              throw new IllegalStateException(
                  "A resource file was found that had two API level qualifiers: " + qualifiersStr);
            }
            result.mWeights[order] = Integer.parseInt(m.group(2));
          } else {
            result.mDefaults.add(qualifier);
          }
        }

        sQualifiersCache.put(qualifiersStr, result);
        return result;
      }
    }
  }
}
