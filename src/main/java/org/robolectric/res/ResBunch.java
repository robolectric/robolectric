package org.robolectric.res;

import java.math.BigInteger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResBunch {

  // Matches a version qualifier like "v14". Parentheses capture the numeric
  // part for easy retrieval with Matcher.group(1).
  private static final String VERSION_QUALIFIER_REGEX = "v([0-9]+)";
  private static final Pattern VERSION_QUALIFIER_PATTERN_WITH_LINE_END =
      Pattern.compile(VERSION_QUALIFIER_REGEX + "$");
  private static final Pattern VERSION_QUALIFIER_PATTERN_WITH_DASHES =
      Pattern.compile("-" + VERSION_QUALIFIER_REGEX + "-");

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

  public static int getVersionQualifierApiLevel(String qualifiers) {
    Matcher m = VERSION_QUALIFIER_PATTERN_WITH_LINE_END.matcher(qualifiers);
    if (m.find()) {
      return Integer.parseInt(m.group(1));
    }
    return -1;
  }

  public static Value pick(Values values, String qualifiers) {
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

    /*
     * If any resources out of the possibles have version qualifiers, return the
     * closest match that doesn't go over. This is the last step because it's lowest
     * in the precedence table at:
     * https://developer.android.com/guide/topics/resources/providing-resources.html#table2
     */
    int targetApiLevel = getVersionQualifierApiLevel(qualifiers);
    if (qualifiers.length() > 0 && targetApiLevel != -1) {
      Value bestMatch = null;
      int bestMatchDistance = Integer.MAX_VALUE;
      for (int i = 0; i < count; i++) {
        if (!possibles.testBit(i)) {
          continue;
        }
        
        Value value = values.get(i);
        int distance = getDistance(value, targetApiLevel);
        if (distance >= 0 && distance < bestMatchDistance) {
          bestMatch = value;
          bestMatchDistance = distance;
        }
      }
      if (bestMatch != null) {
        return bestMatch;
      }
    }

    for (int i = 0; i < count; i++) {
      if (possibles.testBit(i)) return values.get(i);
    }
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
    }
    return distance;
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

    public Value(String qualifiers, TypedResource value, XmlLoader.XmlContext xmlContext) {
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
    
    @Override
    public String toString() {
      return "Value [qualifiers=" + qualifiers + ", value=" + value + ", xmlContext=" + xmlContext
          + "]";
    }
  }

  protected static class Values extends ArrayList<Value> {
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
