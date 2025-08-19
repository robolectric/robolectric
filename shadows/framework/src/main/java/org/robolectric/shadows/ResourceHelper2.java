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

import static org.robolectric.shadows.ResourceHelper.applyUnit;
import static org.robolectric.shadows.ResourceHelper.computeTypedValue;
import static org.robolectric.shadows.ResourceHelper.parseUnit;
import static org.robolectric.shadows.ResourceHelper.sUnitNames;

import android.util.TypedValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Helper class to provide various conversion methods used in handling Android resources. */
public final class ResourceHelper2 {

  private static final Pattern sFloatPattern = Pattern.compile("(-?[0-9]+(?:\\.[0-9]+)?)(.*)");
  private static final float[] sFloatOut = new float[1];

  private static final TypedValue mValue = new TypedValue();

  /**
   * Returns the raw value from the given attribute float-type value string. This object is only
   * valid until the next call on to {@link ResourceHelper2}.
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
   *
   * @param attribute the name of the attribute. Can be null if <var>requireUnit</var> is false.
   * @param value the string value of the attribute
   * @param outValue the TypedValue to receive the parsed value
   * @param requireUnit whether the value is expected to contain a unit.
   * @return true if success.
   */
  public static boolean parseFloatAttribute(
      String attribute, String value, TypedValue outValue, boolean requireUnit) {
    //    assert requireUnit == false || attribute != null;

    // remove the space before and after
    value = value.trim();
    int len = value.length();

    if (len == 0) {
      return false;
    }

    // check that there's no non ascii characters.
    char[] buf = value.toCharArray();
    for (int i = 0; i < len; i++) {
      if (buf[i] > 255) {
        return false;
      }
    }

    // check the first character
    if ((buf[0] < '0' || buf[0] > '9') && buf[0] != '.' && buf[0] != '-') {
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

      if (!end.isEmpty() && end.charAt(0) != ' ') {
        // Might be a unit...
        if (parseUnit(end, outValue, sFloatOut)) {
          computeTypedValue(outValue, f, sFloatOut[0], end);
          return true;
        }
        return false;
      }

      // make sure it's only spaces at the end.
      end = end.trim();

      if (end.isEmpty()) {
        if (outValue != null) {
          outValue.assetCookie = 0;
          outValue.string = null;

          if (!requireUnit) {
            outValue.type = TypedValue.TYPE_FLOAT;
            outValue.data = Float.floatToIntBits(f);
          } else {
            // no unit when required? Use dp and out an error.
            applyUnit(sUnitNames[1], outValue, sFloatOut);
            computeTypedValue(outValue, f, sFloatOut[0], "dp");

            System.out.printf(
                "Dimension \"%1$s\" in attribute \"%2$s\" is missing unit!%n",
                value, attribute == null ? "(unknown)" : attribute);
          }
          return true;
        }
      }
    }

    return false;
  }
}
