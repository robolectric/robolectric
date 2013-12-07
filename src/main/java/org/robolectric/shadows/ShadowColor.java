package org.robolectric.shadows;

import android.graphics.Color;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.fest.reflect.core.Reflection.method;

@Implements(Color.class)
public class ShadowColor {

  @Implementation
  public static int parseColor(String colorString) {
    if (colorString.charAt(0) == '#' && colorString.length() == 4 || colorString.length() == 5) {
      StringBuilder buf = new StringBuilder();
      buf.append('#');
      for (int i = 1; i < colorString.length(); i++) {
        buf.append(colorString.charAt(i));
        buf.append(colorString.charAt(i));
      }
      colorString = buf.toString();
    }
    try {
      return method(RobolectricInternals.directMethodName(Color.class.getName(), "parseColor"))
          .withReturnType(int.class)
          .withParameterTypes(String.class)
          .in(Color.class)
          .invoke(colorString);
    } catch (Exception e) {
      throw new IllegalArgumentException("Can't parse value from color \"" + colorString + "\"", e);
    }
  }
}
