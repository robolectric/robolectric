package org.robolectric.util;

import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// by mindriot -- http://stackoverflow.com/users/1011746/mindriot
// from http://stackoverflow.com/questions/8343971/how-to-parse-a-dimension-string-and-convert-it-to-a-dimension-value
// Apache License 2.0
public class DimensionConverter {

  // -- Initialize dimension string to constant lookup.
  public static final Map<String, Integer> dimensionConstantLookup = initDimensionConstantLookup();
  private static Map<String, Integer> initDimensionConstantLookup() {
    Map<String, Integer> m = new HashMap<String, Integer>();
    m.put("px", TypedValue.COMPLEX_UNIT_PX);
    m.put("dip", TypedValue.COMPLEX_UNIT_DIP);
    m.put("dp", TypedValue.COMPLEX_UNIT_DIP);
    m.put("sp", TypedValue.COMPLEX_UNIT_SP);
    m.put("pt", TypedValue.COMPLEX_UNIT_PT);
    m.put("in", TypedValue.COMPLEX_UNIT_IN);
    m.put("mm", TypedValue.COMPLEX_UNIT_MM);
    return Collections.unmodifiableMap(m);
  }
  // -- Initialize pattern for dimension string.
  private static final Pattern DIMENSION_PATTERN = Pattern.compile("^\\s*(\\d+(\\.\\d+)*)\\s*([a-zA-Z]+)\\s*$");

  public static int stringToDimensionPixelSize(String dimension, DisplayMetrics metrics) {
    // -- Mimics TypedValue.complexToDimensionPixelSize(int data, DisplayMetrics metrics).
    InternalDimension internalDimension = stringToInternalDimension(dimension);
    final float value = internalDimension.value;
    final float f = TypedValue.applyDimension(internalDimension.unit, value, metrics);
    final int res = (int)(f+0.5f);
    if (res != 0) return res;
    if (value == 0) return 0;
    if (value > 0) return 1;
    return -1;
  }

  public static float stringToDimension(String dimension, DisplayMetrics metrics) {
    // -- Mimics TypedValue.complexToDimension(int data, DisplayMetrics metrics).
    InternalDimension internalDimension = stringToInternalDimension(dimension);
    return TypedValue.applyDimension(internalDimension.unit, internalDimension.value, metrics);
  }

  private static InternalDimension stringToInternalDimension(String dimension) {
    // -- Match target against pattern.
    Matcher matcher = DIMENSION_PATTERN.matcher(dimension);
    if (matcher.matches()) {
      // -- Match found.
      // -- Extract value.
      float value = Float.valueOf(matcher.group(1)).floatValue();
      // -- Extract dimension units.
      String unit = matcher.group(3).toLowerCase();
      // -- Get Android dimension constant.
      Integer dimensionUnit = dimensionConstantLookup.get(unit);
      if (dimensionUnit == null) {
        // -- Invalid format.
        throw new NumberFormatException();
      } else {
        // -- Return valid dimension.
        return new InternalDimension(value, dimensionUnit);
      }
    } else {
      // -- Invalid format.
      throw new NumberFormatException();
    }
  }

  private static class InternalDimension {
    float value;
    int unit;

    public InternalDimension(float value, int unit) {
      this.value = value;
      this.unit = unit;
    }
  }
}
