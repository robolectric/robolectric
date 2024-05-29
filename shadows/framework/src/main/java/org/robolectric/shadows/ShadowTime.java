package org.robolectric.shadows;

import android.text.format.Time;
import android.util.TimeFormatException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = Time.class)
public class ShadowTime {
  @RealObject private Time time;

  private static final long SECOND_IN_MILLIS = 1000;
  private static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
  private static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
  private static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

  @Implementation
  protected static int getChar(String s, int spos, int mul) {
    char c = s.charAt(spos);
    if (Character.isDigit(c)) {
      return Character.getNumericValue(c) * mul;
    } else {
      throwTimeFormatException("Parse error at pos=" + spos);
    }
    return -1;
  }

  @Implementation
  protected void checkChar(String s, int spos, char expected) {
    char c = s.charAt(spos);
    if (c != expected) {
      throwTimeFormatException(
          String.format(
              "Unexpected character 0x%02d at pos=%d.  Expected 0x%02d (\'%c\').",
              (int) c, spos, (int) expected, expected));
    }
  }

  private static void throwTimeFormatException(String optionalMessage) {
    throw ReflectionHelpers.callConstructor(
        TimeFormatException.class,
        ReflectionHelpers.ClassParameter.from(
            String.class, optionalMessage == null ? "fail" : optionalMessage));
  }
}
