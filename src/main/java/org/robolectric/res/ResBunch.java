package org.robolectric.res;

import java.math.BigInteger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ResBunch {
  private final Map<String, ResMap<TypedResource>> types = new LinkedHashMap<String, ResMap<TypedResource>>();

  public void put(String attrType, String name, TypedResource value, XmlLoader.XmlContext xmlContext) {
    ResName resName = new ResName(xmlContext.packageName, attrType, name);
    ResMap<TypedResource> valuesMap = getValuesMap(attrType);
    Values values = valuesMap.find(resName);
    values.add(new Value(xmlContext.getQualifiers(), value, xmlContext));
    Collections.sort(values);
  }

  private ResMap<TypedResource> getValuesMap(String attrType) {
    ResMap<TypedResource> valuesMap = types.get(attrType);
    if (valuesMap == null) {
      valuesMap = new ResMap<TypedResource>();
      types.put(attrType, valuesMap);
    }
    return valuesMap;
  }

  public TypedResource get(@NotNull ResName resName, String qualifiers) {
    Value value = getValue(resName, qualifiers);
    return value == null ? null : value.value;
  }

  public Value getValue(@NotNull ResName resName, String qualifiers) {
    ResMap<TypedResource> valuesMap = getValuesMap(resName.type);
    Values values = valuesMap.find(resName);
    return (values != null) ? pick(values, qualifiers) : null;
  }

  public static <T> Value pick(Values values, String qualifiers) {
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
    int size = 0;
    for (ResMap<TypedResource> map : types.values()) {
      size += map.size();
    }
    return size;
  }

  public void makeImmutable() {
    for (ResMap<TypedResource> map : types.values()) {
      map.makeImmutable();
    }
  }

  public void mergeLibraryStyle(ResBunch fromResBundle, String packageName) {
    for (Map.Entry<String, ResMap<TypedResource>> entry : fromResBundle.types.entrySet()) {
      getValuesMap(entry.getKey()).merge(packageName, entry.getValue());
    }
  }

  public static class Value implements Comparable<Value> {
    final String qualifiers;
    final TypedResource value;
    final XmlLoader.XmlContext xmlContext;

    Value(String qualifiers, TypedResource value, XmlLoader.XmlContext xmlContext) {
      if (value == null) {
        throw new NullPointerException();
      }

      this.xmlContext = xmlContext;
      this.qualifiers = qualifiers == null ? "--" : "-" + qualifiers + "-";
      this.value = value;
    }

    @Override
    public int compareTo(Value o) {
      return qualifiers.compareTo(o.qualifiers);
    }

    public TypedResource getTypedResource() {
      return value;
    }

    public XmlLoader.XmlContext getXmlContext() {
      return xmlContext;
    }
  }

  static class Values extends ArrayList<Value> {
  }

  private static class ResMap<T> {
    private final Map<ResName, Values> map = new HashMap<ResName, Values>();
    private boolean immutable;

    public Values find(ResName resName) {
      Values values = map.get(resName);
      if (values == null) map.put(resName, values = new Values());
      return values;
    }

    private void merge(String packageName, ResMap<T> sourceMap) {
      if (immutable) {
        throw new IllegalStateException("immutable!");
      }

      for (Map.Entry<ResName, Values> entry : sourceMap.map.entrySet()) {
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
