package org.robolectric.shadows;

import android.icu.util.ULocale;
import java.util.Locale;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = libcore.icu.ICU.class, isInAndroidSdk = false)
public class ShadowICU {

  @Implementation
  public static String addLikelySubtags(String languageTag) {
    return ULocale.addLikelySubtags(ULocale.forLanguageTag(languageTag)).toLanguageTag();
  }

  @Implementation
  public static String getBestDateTimePattern(String skeleton, Locale locale) {
    switch (skeleton) {
      case "jmm":
        return getjmmPattern(locale);
      case "yMMMd": // This is from {@code DatePickerDefaults.YearAbbrMonthDaySkeleton}
        return "MMM d, y";
      case "yMMMMEEEEd": // This is from {@code DatePickerDefaults.YearMonthWeekdayDaySkeleton}
        return "EEEE, MMMM d, y";
      case "yMMMM": // This is from {@code DatePickerDefaults.YearMonthSkeleton}
        return "MMMM y";
      default:
        return skeleton;
    }
  }

  private static String getjmmPattern(Locale locale) {
    if (locale.equals(new Locale("pt", "BR")) || locale.equals(Locale.UK)) {
      return "H:mm";
    }
    return "h:mm a";
  }
}
