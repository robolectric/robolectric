package org.robolectric.shadows;

import android.content.Context;
import android.text.format.DateFormat;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(DateFormat.class)
public class ShadowDateFormat {
  private static String timeFormatPattern = "HH:mm:ss";
  private static String dateFormatPattern = "MMM-dd-yyyy";
  private static String mediumDateFormatPattern = "MMM dd, yyyy";
  private static String longDateFormatPattern = "MMMMM dd, yyyy";

  public static void setTimeFormatPattern(String pattern) {
    timeFormatPattern = pattern;
  }

  public static void setDateFormatPattern(String pattern) {
    dateFormatPattern = pattern;
  }

  public static void setMediumDateFormatPattern(String pattern) {
    mediumDateFormatPattern = pattern;
  }

  public static void setLongDateFormatPattern(String pattern) {
    longDateFormatPattern = pattern;
  }

  @Implementation
  protected static java.text.DateFormat getDateFormat(Context context) {
    return new java.text.SimpleDateFormat(
        dateFormatPattern, context.getResources().getConfiguration().locale);
  }

  @Implementation
  protected static java.text.DateFormat getMediumDateFormat(Context context) {
    return new java.text.SimpleDateFormat(
        mediumDateFormatPattern, context.getResources().getConfiguration().locale);
  }

  @Implementation
  protected static java.text.DateFormat getLongDateFormat(Context context) {
    return new java.text.SimpleDateFormat(
        longDateFormatPattern, context.getResources().getConfiguration().locale);
  }

  @Implementation
  protected static java.text.DateFormat getTimeFormat(Context context) {
    return new java.text.SimpleDateFormat(
        timeFormatPattern, context.getResources().getConfiguration().locale);
  }
}
