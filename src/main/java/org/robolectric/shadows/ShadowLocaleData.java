package org.robolectric.shadows;

import java.util.Locale;
import libcore.icu.LocaleData;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;
import static org.robolectric.Robolectric.*;

@Implements(value = Robolectric.Anything.class, className = ShadowLocaleData.REAL_CLASS_NAME)
public class ShadowLocaleData {
  public static final String REAL_CLASS_NAME = "libcore.icu.LocaleData";

  @Implementation
  public static Object get(Locale locale) {
    LocaleData localeData = (LocaleData) newInstanceOf(REAL_CLASS_NAME);

    if (locale == null) {
      locale = Locale.getDefault();
    }

    if (locale.equals(Locale.US)) {
      localeData.amPm = new String[]{"AM", "PM"};
      localeData.eras = new String[]{"BC", "AD"};

      localeData.firstDayOfWeek = 1;
      localeData.minimalDaysInFirstWeek = 1;

      localeData.longMonthNames = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
      localeData.shortMonthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
      localeData.tinyMonthNames = new String[]{"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};

      localeData.longStandAloneMonthNames = localeData.longMonthNames;
      localeData.shortStandAloneMonthNames = localeData.shortMonthNames;
      localeData.tinyStandAloneMonthNames = localeData.tinyMonthNames;

      localeData.longWeekdayNames = new String[]{"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
      localeData.shortWeekdayNames = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
      localeData.tinyWeekdayNames = new String[]{"", "S", "M", "T", "W", "T", "F", "S"};

      localeData.longStandAloneWeekdayNames = localeData.longWeekdayNames;
      localeData.shortStandAloneWeekdayNames = localeData.shortWeekdayNames;
      localeData.tinyStandAloneWeekdayNames = localeData.tinyWeekdayNames;

      localeData.yesterday = "Yesterday";
      localeData.today = "Today";
      localeData.tomorrow = "Tomorrow";

      localeData.fullTimeFormat = "h:mm:ss a zzzz";
      localeData.longTimeFormat = "h:mm:ss a z";
      localeData.mediumTimeFormat = "h:mm:ss a";
      localeData.shortTimeFormat = "h:mm a";
      localeData.timeFormat12 = "h:mm a";
      localeData.timeFormat24 = "HH:mm";

      localeData.fullDateFormat = "EEEE, MMMM d, y";
      localeData.longDateFormat = "MMMM d, y";
      localeData.mediumDateFormat = "MMM d, y";
      localeData.shortDateFormat = "M/d/yy";

      localeData.zeroDigit = '0';
      localeData.decimalSeparator = '.';
      localeData.groupingSeparator = ',';
      localeData.patternSeparator = ';';
      localeData.percent = '%';
      localeData.perMill = '‰';
      localeData.monetarySeparator = '.';
      localeData.minusSign = '-';

      localeData.exponentSeparator = "E";
      localeData.infinity = "∞";
      localeData.NaN = "NaN";

      localeData.currencySymbol = "$";
      localeData.internationalCurrencySymbol = "USD";

      localeData.numberPattern = "#,##0.###";
      localeData.integerPattern = "#,##0";
      localeData.currencyPattern = "¤#,##0.00;(¤#,##0.00)";
      localeData.percentPattern = "#,##0%";
    }

    return localeData;
  }
}
