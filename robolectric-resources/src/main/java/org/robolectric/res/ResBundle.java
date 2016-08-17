package org.robolectric.res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResBundle<T> {


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

  public static <T> Value<T> pick(List<Value<T>> values, String qualifiersStr) {
    final int count = values.size();
    if (count == 0) return null;

    // This should really follow the android algorithm specified at:
    // http://developer.android.com/guide/topics/resources/providing-resources.html#BestMatch
    //
    // 1: eliminate resources that contradict the qualifiersStr
    // 2: pick the (next) highest-precedence qualifier type in "table 2" of the reference above
    // 3: check if any resource values use this qualifier, if no, back to 2, else move on to 4.
    // 4: eliminate resources values that don't use this qualifier.
    // 5: if more than one resource is left, go back to 2.
    //
    // However, we currently only model the smallest/available width/height and version qualifiers
    // rather than all of the possibly qualifier classes in table 2.

    Qualifiers toMatch = Qualifiers.parse(qualifiersStr);

    Qualifiers bestMatchQualifiers = null;
    Value<T> bestMatch = null;

    List<Value<T>> passesRequirements = new ArrayList<>();
    for (Value<T> value : values) {
      Qualifiers qualifiers = Qualifiers.parse(value.qualifiers);
      if (qualifiers.passesRequirements(toMatch)) {
        passesRequirements.add(value);
      }
    }

    for (Value<T> value : passesRequirements) {
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
    }
    if (!passesRequirements.isEmpty()) {
      return passesRequirements.get(0);
    }
    return null;
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
