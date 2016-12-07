package org.robolectric.res;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResBundle {
  private final ResMap valuesMap = new ResMap();
  private String overrideNamespace;

  public void put(String attrType, String name, TypedResource value) {
    XmlContext xmlContext = value.getXmlContext();
    ResName resName = new ResName(maybeOverride(xmlContext.packageName), attrType, name);
    List<TypedResource> values = valuesMap.find(resName);
    values.add(value);

    // todo: should sort once we're fully populated, not now
    Collections.sort(values, new Comparator<TypedResource>() {
      @Override
      public int compare(TypedResource o1, TypedResource o2) {
        return o1.getQualifiers().compareTo(o2.getQualifiers());
      }
    });
  }

  public TypedResource get(ResName resName, String qualifiers) {
    List<TypedResource> typedResources = valuesMap.find(maybeOverride(resName));
    return pick(typedResources, qualifiers);
  }

  public static TypedResource pick(List<TypedResource> typedResources, String qualifiersStr) {
    final int count = typedResources.size();
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

    List<TypedResource> passesRequirements = new ArrayList<>();
    for (TypedResource candidate : typedResources) {
      Qualifiers qualifiers = Qualifiers.parse(candidate.getQualifiers());
      if (qualifiers.passesRequirements(toMatch)) {
        passesRequirements.add(candidate);
      }
    }

    Qualifiers bestMatchQualifiers = null;
    TypedResource bestMatch = null;
    for (TypedResource candidate : passesRequirements) {
      Qualifiers qualifiers = Qualifiers.parse(candidate.getQualifiers());
      if (qualifiers.matches(toMatch)) {
        if (bestMatchQualifiers == null || qualifiers.isBetterThan(bestMatchQualifiers, toMatch)) {
          bestMatchQualifiers = qualifiers;
          bestMatch =  candidate;
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
    return valuesMap.size();
  }

  public void makeImmutable() {
    valuesMap.makeImmutable();
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

  public void mergeLibraryStyle(ResBundle fromResBundle, String packageName) {
    valuesMap.merge(packageName, fromResBundle.valuesMap);
  }

  public void receive(ResourceLoader.Visitor visitor) {
    for (final Map.Entry<ResName, List<TypedResource>> entry : valuesMap.map.entrySet()) {
      visitor.visit(entry.getKey(), new AbstractList<TypedResource>() {
        List<TypedResource> typedResources;

        @Override
        public TypedResource get(int index) {
          if (typedResources == null) typedResources = entry.getValue();
          return typedResources.get(index);
        }

        @Override
        public int size() {
          if (typedResources == null) typedResources = entry.getValue();
          return typedResources.size();
        }
      });
    }
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

  private static class ResMap {
    private final Map<ResName, List<TypedResource>> map = new HashMap<>();
    private boolean immutable;

    public List<TypedResource> find(ResName resName) {
      List<TypedResource> values = map.get(resName);
      if (values == null) map.put(resName, values = new ArrayList<>());
      return values;
    }

    private void merge(String packageName, ResMap sourceMap) {
      if (immutable) {
        throw new IllegalStateException("immutable!");
      }

      for (Map.Entry<ResName, List<TypedResource>> entry : sourceMap.map.entrySet()) {
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
