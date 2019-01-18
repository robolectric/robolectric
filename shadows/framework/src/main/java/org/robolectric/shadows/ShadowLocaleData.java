package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import java.util.Locale;
import libcore.icu.LocaleData;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Robolectric only supports en_US regardless of the default locale set in the JVM.
 */
@Implements(value = LocaleData.class, isInAndroidSdk = false)
public class ShadowLocaleData {
  public static final String REAL_CLASS_NAME = "libcore.icu.LocaleData";

  @Implementation
  public static LocaleData get(Locale locale) {
    LocaleData localeData = (LocaleData) Shadow.newInstanceOf(REAL_CLASS_NAME);
    if (locale == null) {
      locale = Locale.getDefault();
    }
    setEnUsLocaleData(localeData);
    return localeData;
  }

  private static void setEnUsLocaleData(LocaleData localeData) {
    localeData.amPm = new String[]{"AM", "PM"};
    localeData.eras = new String[]{"BC", "AD"};

    localeData.firstDayOfWeek = 1;
    localeData.minimalDaysInFirstWeek = 1;

    localeData.longMonthNames = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    localeData.shortMonthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    if (getApiLevel() >= JELLY_BEAN_MR1) {
      localeData.tinyMonthNames = new String[]{"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};
      localeData.tinyStandAloneMonthNames = localeData.tinyMonthNames;
      localeData.tinyWeekdayNames = new String[]{"", "S", "M", "T", "W", "T", "F", "S"};
      localeData.tinyStandAloneWeekdayNames = localeData.tinyWeekdayNames;

      localeData.yesterday = "Yesterday";
      localeData.today = "Today";
      localeData.tomorrow = "Tomorrow";
    }

    localeData.longStandAloneMonthNames = localeData.longMonthNames;
    localeData.shortStandAloneMonthNames = localeData.shortMonthNames;

    localeData.longWeekdayNames = new String[]{"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    localeData.shortWeekdayNames = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    localeData.longStandAloneWeekdayNames = localeData.longWeekdayNames;
    localeData.shortStandAloneWeekdayNames = localeData.shortWeekdayNames;

    localeData.fullTimeFormat = "h:mm:ss a zzzz";
    localeData.longTimeFormat = "h:mm:ss a z";
    localeData.mediumTimeFormat = "h:mm:ss a";
    localeData.shortTimeFormat = "h:mm a";

    _LocaleData_ _localeData_ = reflector(_LocaleData_.class, localeData);
    if (getApiLevel() >= M) {
      localeData.timeFormat_hm = "h:mm a";
      localeData.timeFormat_Hm = "HH:mm";
    } else if (getApiLevel() >= JELLY_BEAN_MR2) {
      _localeData_.setTimeFormat12("h:mm a");
      _localeData_.setTimeFormat24("HH:mm");
    }

    localeData.fullDateFormat = "EEEE, MMMM d, y";
    localeData.longDateFormat = "MMMM d, y";
    localeData.mediumDateFormat = "MMM d, y";
    localeData.shortDateFormat = "M/d/yy";

    localeData.zeroDigit = '0';
    localeData.decimalSeparator = '.';
    localeData.groupingSeparator = ',';
    localeData.patternSeparator = ';';

    if (getApiLevel() >= LOLLIPOP_MR1) {
      // Lollipop MR1 uses a String
      localeData.percent = "%";
    } else {
      // Upto Lollipop was a char
      _localeData_.setPercent('%');
    }

    if (getApiLevel() >= android.os.Build.VERSION_CODES.P) {
      // P uses a String
      localeData.perMill = "‰";
    } else {
      // Up to P was a char
      _localeData_.setPerMill('‰');
    }

    localeData.monetarySeparator = '.';

    if (getApiLevel() >= LOLLIPOP) {
      // Lollipop uses a String
      localeData.minusSign = "-";
    } else {
      // Upto KitKat was a char
      _localeData_.setMinusSign('-');
    }

    localeData.exponentSeparator = "E";
    localeData.infinity = "\u221E";
    localeData.NaN = "NaN";

    localeData.currencySymbol = "$";
    localeData.internationalCurrencySymbol = "USD";

    localeData.numberPattern = "\u0023,\u0023\u00230.\u0023\u0023\u0023";
    localeData.integerPattern = "\u0023,\u0023\u00230";
    localeData.currencyPattern = "\u00A4\u0023,\u0023\u00230.00;(\u00A4\u0023,\u0023\u00230.00)";
    localeData.percentPattern = "\u0023,\u0023\u00230%";
  }

  /** Accessor interface for {@link LocaleData}'s internals. */
  @ForType(LocaleData.class)
  interface _LocaleData_ {

    @Accessor("minusSign")
    void setMinusSign(char c);

    @Accessor("percent")
    void setPercent(char c);

    @Accessor("perMill")
    void setPerMill(char c);

    @Accessor("timeFormat12")
    void setTimeFormat12(String format);

    @Accessor("timeFormat24")
    void setTimeFormat24(String format);
  }
}
