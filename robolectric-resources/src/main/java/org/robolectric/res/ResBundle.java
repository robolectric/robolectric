package org.robolectric.res;

import java.util.*;

public class ResBundle {
  private final ResMap valuesMap = new ResMap();

  public void put(String attrType, String name, TypedResource value) {
    XmlLoader.XmlContext xmlContext = value.getXmlContext();
    ResName resName = new ResName(xmlContext.getPackageName(), attrType, name);
    valuesMap.put(resName, value);
  }

  public TypedResource get(ResName resName, String qualifiers) {
    return valuesMap.pick(resName, qualifiers);
  }

  public void receive(ResourceProvider.Visitor visitor) {
    for (final Map.Entry<ResName, Map<String, TypedResource>> entry : valuesMap.map.entrySet()) {
      visitor.visit(entry.getKey(), entry.getValue().values());
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

  static class ResMap {
    private final Map<ResName, Map<String, TypedResource>> map = new HashMap<>();

    public TypedResource pick(ResName resName, String qualifiersStr) {
      Map<String, TypedResource> values = map.get(resName);
      if (values == null || values.size() == 0) return null;

      TreeSet<TypedResource> typedResources = new TreeSet<>(new QualifierSort());
      typedResources.addAll(values.values());

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

    public void put(ResName resName, TypedResource value) {
      Map<String, TypedResource> values = map.get(resName);
      if (values == null) map.put(resName, values = new HashMap<>());
      if (!values.containsKey(value.getQualifiers())) {
        values.put(value.getQualifiers(), value);
      }
    }

    public int size() {
      return map.size();
    }

    public static class QualifierSort implements Comparator<TypedResource> {
      @Override
      public int compare(TypedResource o1, TypedResource o2) {
        return o1.getQualifiers().compareTo(o2.getQualifiers());
      }
    }
  }
}
