package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S_V2;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import java.util.Locale;
import libcore.icu.LocaleData;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Robolectric only supports en_US regardless of the default locale set in the JVM. */
@Implements(value = LocaleData.class, isInAndroidSdk = false, maxSdk = S_V2)
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
    localeData.amPm = new String[] {"AM", "PM"};
    localeData.eras = new String[] {"BC", "AD"};

    localeData.firstDayOfWeek = 1;
    localeData.minimalDaysInFirstWeek = 1;

    localeData.longMonthNames =
        new String[] {
          "January",
          "February",
          "March",
          "April",
          "May",
          "June",
          "July",
          "August",
          "September",
          "October",
          "November",
          "December"
        };
    localeData.shortMonthNames =
        new String[] {
          "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

    _LocaleData_ localDataReflector = reflector(_LocaleData_.class, localeData);
    if (getApiLevel() >= JELLY_BEAN_MR1) {
      localeData.tinyMonthNames =
          new String[] {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"};
      localeData.tinyStandAloneMonthNames = localeData.tinyMonthNames;
      localeData.tinyWeekdayNames = new String[] {"", "S", "M", "T", "W", "T", "F", "S"};
      localeData.tinyStandAloneWeekdayNames = localeData.tinyWeekdayNames;

      if (getApiLevel() <= R) {
        localDataReflector.setYesterday("Yesterday");
      }
      localeData.today = "Today";
      localeData.tomorrow = "Tomorrow";
    }

    localeData.longStandAloneMonthNames = localeData.longMonthNames;
    localeData.shortStandAloneMonthNames = localeData.shortMonthNames;

    localeData.longWeekdayNames =
        new String[] {
          "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        };
    localeData.shortWeekdayNames =
        new String[] {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    localeData.longStandAloneWeekdayNames = localeData.longWeekdayNames;
    localeData.shortStandAloneWeekdayNames = localeData.shortWeekdayNames;

    localDataReflector.setFullTimeFormat("h:mm:ss a zzzz");
    localDataReflector.setLongTimeFormat("h:mm:ss a z");
    localDataReflector.setMediumTimeFormat("h:mm:ss a");
    localDataReflector.setShortTimeFormat("h:mm a");

    if (getApiLevel() >= M) {
      localeData.timeFormat_hm = "h:mm a";
      localeData.timeFormat_Hm = "HH:mm";
    } else if (getApiLevel() >= JELLY_BEAN_MR2) {
      localDataReflector.setTimeFormat12("h:mm a");
      localDataReflector.setTimeFormat24("HH:mm");
    }

    localDataReflector.setFullDateFormat("EEEE, MMMM d, y");
    localDataReflector.setLongDateFormat("MMMM d, y");
    localDataReflector.setMediumDateFormat("MMM d, y");
    localDataReflector.setShortDateFormat("M/d/yy");
    if (getApiLevel() >= KITKAT && getApiLevel() < M) {
      localDataReflector.setShortDateFormat4("M/d/yyyy");
    }

    localeData.zeroDigit = '0';
    localDataReflector.setDecimalSeparator('.');
    localDataReflector.setGroupingSeparator(',');
    localDataReflector.setPatternSeparator(';');

    if (getApiLevel() >= LOLLIPOP_MR1) {
      // Lollipop MR1 uses a String
      localDataReflector.setPercent("%");
    } else {
      // Upto Lollipop was a char
      localDataReflector.setPercent('%');
    }

    if (getApiLevel() >= android.os.Build.VERSION_CODES.P) {
      // P uses a String
      localDataReflector.setPerMill("\u2030"); // '‰'
    } else {
      // Up to P was a char
      localDataReflector.setPerMill('\u2030'); // '‰'
    }

    localDataReflector.setMonetarySeparator('.');

    if (getApiLevel() >= LOLLIPOP) {
      // Lollipop uses a String
      localDataReflector.setMinusSign("-");
    } else {
      // Upto KitKat was a char
      localDataReflector.setMinusSign('-');
    }

    localDataReflector.setExponentSeparator("E");
    localDataReflector.setInfinity("\u221E");
    localDataReflector.setNaN("NaN");

    if (getApiLevel() <= R) {
      localDataReflector.setCurrencySymbol("$");
      localDataReflector.setInternationalCurrencySymbol("USD");
    }

    localDataReflector.setNumberPattern("\u0023,\u0023\u00230.\u0023\u0023\u0023");
    localDataReflector.setIntegerPattern("\u0023,\u0023\u00230");
    localDataReflector.setCurrencyPattern(
        "\u00A4\u0023,\u0023\u00230.00;(\u00A4\u0023,\u0023\u00230.00)");
    localDataReflector.setPercentPattern("\u0023,\u0023\u00230%");
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

    @Accessor("shortDateFormat4")
    void setShortDateFormat4(String format);

    // <= R
    @Accessor("yesterday")
    void setYesterday(String yesterday);

    // <= R
    @Accessor("currencySymbol")
    void setCurrencySymbol(String symbol);

    // <= R
    @Accessor("internationalCurrencySymbol")
    void setInternationalCurrencySymbol(String symbol);

    // <= S_V2
    @Accessor("fullTimeFormat")
    void setFullTimeFormat(String symbol);

    // <= S_V2
    @Accessor("longTimeFormat")
    void setLongTimeFormat(String symbol);

    // <= S_V2
    @Accessor("mediumTimeFormat")
    void setMediumTimeFormat(String symbol);

    // <= S_V2
    @Accessor("shortTimeFormat")
    void setShortTimeFormat(String symbol);

    // <= S_V2
    @Accessor("fullDateFormat")
    void setFullDateFormat(String symbol);

    // <= S_V2
    @Accessor("longDateFormat")
    void setLongDateFormat(String symbol);

    // <= S_V2
    @Accessor("mediumDateFormat")
    void setMediumDateFormat(String symbol);

    // <= S_V2
    @Accessor("shortDateFormat")
    void setShortDateFormat(String symbol);

    // <= S_V2
    @Accessor("decimalSeparator")
    void setDecimalSeparator(char symbol);

    // <= S_V2
    @Accessor("groupingSeparator")
    void setGroupingSeparator(char symbol);

    // <= S_V2
    @Accessor("patternSeparator")
    void setPatternSeparator(char symbol);

    // <= S_V2
    @Accessor("percent")
    void setPercent(String symbol);

    // <= S_V2
    @Accessor("perMill")
    void setPerMill(String symbol);

    // <= S_V2
    @Accessor("monetarySeparator")
    void setMonetarySeparator(char symbol);

    // <= S_V2
    @Accessor("minusSign")
    void setMinusSign(String symbol);

    // <= S_V2
    @Accessor("exponentSeparator")
    void setExponentSeparator(String symbol);

    // <= S_V2
    @Accessor("infinity")
    void setInfinity(String symbol);

    // <= S_V2
    @Accessor("NaN")
    void setNaN(String symbol);

    // <= S_V2
    @Accessor("numberPattern")
    void setNumberPattern(String symbol);

    // <= S_V2
    @Accessor("integerPattern")
    void setIntegerPattern(String symbol);

    // <= S_V2
    @Accessor("currencyPattern")
    void setCurrencyPattern(String symbol);

    // <= S_V2
    @Accessor("percentPattern")
    void setPercentPattern(String symbol);
  }
}

