package org.robolectric.res;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ResBundle<T> {
  private final ResMap<T> valuesMap = new ResMap<T>();
  private final ResMap<List<T>> valuesArrayMap = new ResMap<List<T>>();
  private String overrideNamespace;

  public void put(String attrType, String name, T value, XmlLoader.XmlContext xmlContext) {
    ResName resName = new ResName(maybeOverride(xmlContext.packageName), attrType, name);
    Values<T> values = valuesMap.find(resName);
    values.add(new Value<T>(xmlContext.getQualifiers(), value, xmlContext));
    Collections.sort(values);
  }

  public T get(ResName resName, String qualifiers) {
    Value<T> value = getValue(resName, qualifiers);
    return value == null ? null : value.value;
  }

  public Value<T> getValue(ResName resName, String qualifiers) {
    Values<T> values = valuesMap.find(maybeOverride(resName));
    return (values != null) ? pick(values, qualifiers) : null;
  }

  public static <T> Value<T> pick(Values<T> values, String qualifiers) {
    final int count = values.size();
    if (count == 0) return null;

    BigInteger possibles = BigInteger.ZERO;
    for (int i = 0; i < count; i++) possibles = possibles.setBit(i);

    StringTokenizer st = new StringTokenizer(qualifiers, "-");
    while (st.hasMoreTokens()) {
      String qualifier = st.nextToken();
      String paddedQualifier = "-" + qualifier + "-";
      BigInteger matches = BigInteger.ZERO;

      for (int i = 0; i < count; i++) {
        if (!possibles.testBit(i)) continue;

        if (values.get(i).qualifiers.contains(paddedQualifier)) {
          matches = matches.setBit(i);
        }
      }

      if (!matches.equals(BigInteger.ZERO)) {
        possibles = possibles.and(matches); // eliminate any that didn't match this qualifier
      }

      if (matches.bitCount() == 1) break;
    }

    for (int i = 0; i < count; i++) {
      if (possibles.testBit(i)) return values.get(i);
    }
    throw new IllegalStateException("couldn't handle qualifiers \"" + qualifiers + "\"");
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

  static class Value<T> implements Comparable<Value<T>> {
    final String qualifiers;
    final T value;
    final XmlLoader.XmlContext xmlContext;

    Value(String qualifiers, T value, XmlLoader.XmlContext xmlContext) {
      if (value == null) {
        throw new NullPointerException();
      }

      this.xmlContext = xmlContext;
      this.qualifiers = qualifiers == null ? "--" : "-" + qualifiers + "-";
      this.value = value;
    }

    @Override
    public int compareTo(Value<T> o) {
      return qualifiers.compareTo(o.qualifiers);
    }
  }

  static class Values<T> extends ArrayList<Value<T>> {
  }

  private static class ResMap<T> {
    private final Map<ResName, Values<T>> map = new HashMap<ResName, Values<T>>();
    private boolean immutable;

    public Values<T> find(ResName resName) {
      Values<T> values = map.get(resName);
      if (values == null) map.put(resName, values = new Values<T>());
      return values;
    }

    private void merge(String packageName, ResMap<T> sourceMap) {
      if (immutable) {
        throw new IllegalStateException("immutable!");
      }

      for (Map.Entry<ResName, Values<T>> entry : sourceMap.map.entrySet()) {
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
