package org.robolectric.shadows;

import android.content.Context;
import android.text.format.DateFormat;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Locale;

@Implements(DateFormat.class)
public class ShadowDateFormat {

  @Implementation
  public static java.text.DateFormat getDateFormat(Context context) {
    return new java.text.SimpleDateFormat("MMM-dd-yyyy", Locale.ROOT);
  }

  @Implementation
  public static java.text.DateFormat getLongDateFormat(Context context) {
    return new java.text.SimpleDateFormat("MMMM dd, yyyy", Locale.ROOT);
  }

  @Implementation
  public static java.text.DateFormat getTimeFormat(Context context) {
    return new java.text.SimpleDateFormat("HH:mm:ss", Locale.ROOT);
  }
}
