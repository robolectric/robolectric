/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.shadows;

import android.util.TypedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.ResName;

/**
 * Helper class to provide various conversion method used in handling android resources.
 */
public final class ResourceHelper2 {

  private final static Pattern sFloatPattern = Pattern.compile("(-?[0-9]+(?:\\.[0-9]+)?)(.*)");
  private final static float[] sFloatOut = new float[1];

  private final static TypedValue mValue = new TypedValue();

  // ------- TypedValue stuff
  // This is taken from //device/libs/utils/ResourceTypes.cpp

  private static final class UnitEntry {
    String name;
    int type;
    int unit;
    float scale;

    UnitEntry(String name, int type, int unit, float scale) {
      this.name = name;
      this.type = type;
      this.unit = unit;
      this.scale = scale;
    }
  }

  private final static UnitEntry[] sUnitNames = new UnitEntry[] {
    new UnitEntry("px", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_PX, 1.0f),
    new UnitEntry("dip", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_DIP, 1.0f),
    new UnitEntry("dp", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_DIP, 1.0f),
    new UnitEntry("sp", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_SP, 1.0f),
    new UnitEntry("pt", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_PT, 1.0f),
    new UnitEntry("in", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_IN, 1.0f),
    new UnitEntry("mm", TypedValue.TYPE_DIMENSION, TypedValue.COMPLEX_UNIT_MM, 1.0f),
    new UnitEntry("%", TypedValue.TYPE_FRACTION, TypedValue.COMPLEX_UNIT_FRACTION, 1.0f/100),
    new UnitEntry("%p", TypedValue.TYPE_FRACTION, TypedValue.COMPLEX_UNIT_FRACTION_PARENT, 1.0f/100),
  };

  /**
   * Returns the raw value from the given attribute float-type value string.
   * This object is only valid until the next call on to {@link ResourceHelper2}.
   *
   * @param attribute Attribute name.
   * @param value Attribute value.
   * @param requireUnit whether the value is expected to contain a unit.
   * @return The typed value.
   */
  public static TypedValue getValue(String attribute, String value, boolean requireUnit) {
    if (parseFloatAttribute(attribute, value, mValue, requireUnit)) {
      return mValue;
    }

    return null;
  }

  /**
   * Parse a float attribute and return the parsed value into a given TypedValue.
   * @param attribute the name of the attribute. Can be null if <var>requireUnit</var> is false.
   * @param value the string value of the attribute
   * @param outValue the TypedValue to receive the parsed value
   * @param requireUnit whether the value is expected to contain a unit.
   * @return true if success.
   */
  public static boolean parseFloatAttribute(String attribute, String value,
      TypedValue outValue, boolean requireUnit) {
//    assert requireUnit == false || attribute != null;

    // remove the space before and after
    value = value.trim();
    int len = value.length();

    if (len <= 0) {
      return false;
    }

    // check that there's no non ascii characters.
    char[] buf = value.toCharArray();
    for (int i = 0 ; i < len ; i++) {
      if (buf[i] > 255) {
        return false;
      }
    }

    // check the first character
    if (buf[0] < '0' && buf[0] > '9' && buf[0] != '.' && buf[0] != '-') {
      return false;
    }

    // now look for the string that is after the float...
    Matcher m = sFloatPattern.matcher(value);
    if (m.matches()) {
      String f_str = m.group(1);
      String end = m.group(2);

      float f;
      try {
        f = Float.parseFloat(f_str);
      } catch (NumberFormatException e) {
        // this shouldn't happen with the regexp above.
        return false;
      }

      if (end.length() > 0 && end.charAt(0) != ' ') {
        // Might be a unit...
        if (parseUnit(end, outValue, sFloatOut)) {
          computeTypedValue(outValue, f, sFloatOut[0], end);
          return true;
        }
        return false;
      }

      // make sure it's only spaces at the end.
      end = end.trim();

      if (end.length() == 0) {
        if (outValue != null) {
          outValue.assetCookie = 0;
          outValue.string = null;

          if (requireUnit == false) {
            outValue.type = TypedValue.TYPE_FLOAT;
            outValue.data = Float.floatToIntBits(f);
          } else {
            // no unit when required? Use dp and out an error.
            applyUnit(sUnitNames[1], outValue, sFloatOut);
            computeTypedValue(outValue, f, sFloatOut[0], "dp");

            System.out.println(String.format(
                "Dimension \"%1$s\" in attribute \"%2$s\" is missing unit!",
                    value, attribute == null ? "(unknown)" : attribute));
          }
          return true;
        }
      }
    }

    return false;
  }

  private static void computeTypedValue(TypedValue outValue, float value, float scale, String unit) {
    value *= scale;
    boolean neg = value < 0;
    if (neg) {
      value = -value;
    }
    long bits = (long)(value*(1<<23)+.5f);
    int radix;
    int shift;
    if ((bits&0x7fffff) == 0) {
      // Always use 23p0 if there is no fraction, just to make
      // things easier to read.
      radix = TypedValue.COMPLEX_RADIX_23p0;
      shift = 23;
    } else if ((bits&0xffffffffff800000L) == 0) {
      // Magnitude is zero -- can fit in 0 bits of precision.
      radix = TypedValue.COMPLEX_RADIX_0p23;
      shift = 0;
    } else if ((bits&0xffffffff80000000L) == 0) {
      // Magnitude can fit in 8 bits of precision.
      radix = TypedValue.COMPLEX_RADIX_8p15;
      shift = 8;
    } else if ((bits&0xffffff8000000000L) == 0) {
      // Magnitude can fit in 16 bits of precision.
      radix = TypedValue.COMPLEX_RADIX_16p7;
      shift = 16;
    } else {
      // Magnitude needs entire range, so no fractional part.
      radix = TypedValue.COMPLEX_RADIX_23p0;
      shift = 23;
    }
    int mantissa = (int)(
      (bits>>shift) & TypedValue.COMPLEX_MANTISSA_MASK);
    if (neg) {
      mantissa = (-mantissa) & TypedValue.COMPLEX_MANTISSA_MASK;
    }
    outValue.data |=
      (radix<<TypedValue.COMPLEX_RADIX_SHIFT)
      | (mantissa<<TypedValue.COMPLEX_MANTISSA_SHIFT);

    if ("%".equals(unit)) {
      value = value * 100;
    }

    outValue.string = value + unit;
  }

  private static boolean parseUnit(String str, TypedValue outValue, float[] outScale) {
    str = str.trim();

    for (UnitEntry unit : sUnitNames) {
      if (unit.name.equals(str)) {
        applyUnit(unit, outValue, outScale);
        return true;
      }
    }

    return false;
  }

  private static void applyUnit(UnitEntry unit, TypedValue outValue, float[] outScale) {
    outValue.type = unit.type;
    outValue.data = unit.unit << TypedValue.COMPLEX_UNIT_SHIFT;
    outScale[0] = unit.scale;
  }
}

