package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import java.util.Locale;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = libcore.icu.ICU.class, isInAndroidSdk = false)
public class ShadowICU {

  @Implementation
  public static String addLikelySubtags(String locale) {
    return "en-US";
  }

  @Implementation(minSdk = LOLLIPOP)
  public static String getBestDateTimePattern(String skeleton, Locale locale) {
    switch (skeleton) {
      case "jmm":
        return getjmmPattern(locale);
      default:
        return skeleton;
    }
  }

  @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = KITKAT_WATCH)
  public static String getBestDateTimePattern(String skeleton, String locale) {
    return skeleton;
  }

  private static String getjmmPattern(Locale locale) {
    if (locale.equals(new Locale("pt", "BR")) || locale.equals(Locale.UK)) {
      return "H:mm";
    }
    return "h:mm a";
  }
}
