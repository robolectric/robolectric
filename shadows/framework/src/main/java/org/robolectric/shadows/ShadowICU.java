package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.icu.util.ULocale;
import java.util.Locale;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = libcore.icu.ICU.class, isInAndroidSdk = false)
public class ShadowICU {

  @Implementation
  public static String addLikelySubtags(String languageTag) {
    if (RuntimeEnvironment.getApiLevel() >= N) {
      return ULocale.addLikelySubtags(ULocale.forLanguageTag(languageTag)).toLanguageTag();
    } else {
      // Return what is essentially the given locale, normalized by passing through the Locale
      // factory method.
      Locale locale = Locale.forLanguageTag(languageTag);
      // To support testing with the ar-XB pseudo locale add "Arab" as the script for "ar" language,
      // this is used by the Configuration to set the layout direction.
      if (locale.getScript().isEmpty()
          && locale.getLanguage().equals(new Locale("ar").getLanguage())) {
        locale = new Locale.Builder().setLanguageTag(languageTag).setScript("Arab").build();
      }
      return locale.toLanguageTag();
    }
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
