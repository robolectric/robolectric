package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S_V2;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import libcore.icu.LocaleData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@RunWith(AndroidJUnit4.class)
@Config(maxSdk = S_V2)
public class ShadowLocaleDataTest {

  @Test
  public void shouldSupportLocaleEn_US() throws NoSuchFieldException, IllegalAccessException {
    LocaleData localeData = LocaleData.get(Locale.US);
    LocaleDataReflector localeDataReflector = reflector(LocaleDataReflector.class, localeData);
    assertThat(localeData.amPm).isEqualTo(new String[] {"AM", "PM"});
    assertThat(localeData.eras).isEqualTo(new String[] {"BC", "AD"});

    assertThat(localeData.firstDayOfWeek).isEqualTo(1);
    assertThat(localeData.minimalDaysInFirstWeek).isEqualTo(1);

    assertThat(localeData.longMonthNames)
        .isEqualTo(
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
            });
    assertThat(localeData.shortMonthNames)
        .isEqualTo(
            new String[] {
              "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            });

    assertThat(localeData.longStandAloneMonthNames).isEqualTo(localeData.longMonthNames);
    assertThat(localeData.shortStandAloneMonthNames).isEqualTo(localeData.shortMonthNames);

    assertThat(localeData.longWeekdayNames)
        .isEqualTo(
            new String[] {
              "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
            });
    assertThat(localeData.shortWeekdayNames)
        .isEqualTo(new String[] {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});

    assertThat(localeData.longStandAloneWeekdayNames).isEqualTo(localeData.longWeekdayNames);
    assertThat(localeData.shortStandAloneWeekdayNames).isEqualTo(localeData.shortWeekdayNames);

    assertThat(localeDataReflector.getFullTimeFormat()).isEqualTo("h:mm:ss a zzzz");
    assertThat(localeDataReflector.getLongTimeFormat()).isEqualTo("h:mm:ss a z");
    assertThat(localeDataReflector.getMediumTimeFormat()).isEqualTo("h:mm:ss a");
    assertThat(localeDataReflector.getShortTimeFormat()).isEqualTo("h:mm a");

    assertThat(localeDataReflector.getFullDateFormat()).isEqualTo("EEEE, MMMM d, y");
    assertThat(localeDataReflector.getLongDateFormat()).isEqualTo("MMMM d, y");
    assertThat(localeDataReflector.getMediumDateFormat()).isEqualTo("MMM d, y");
    assertThat(localeDataReflector.getShortDateFormat()).isEqualTo("M/d/yy");

    assertThat(localeData.zeroDigit).isEqualTo('0');
    assertThat(localeDataReflector.getDecimalSeparator()).isEqualTo('.');
    assertThat(localeDataReflector.getGroupingSeparator()).isEqualTo(',');
    assertThat(localeDataReflector.getPatternSeparator()).isEqualTo(';');

    assertThat(localeDataReflector.getMonetarySeparator()).isEqualTo('.');

    assertThat(localeDataReflector.getExponentSeparator()).isEqualTo("E");
    assertThat(localeDataReflector.getInfinity()).isEqualTo("∞");
    assertThat(localeDataReflector.getNaN()).isEqualTo("NaN");

    if (getApiLevel() <= R) {
      assertThat(localeDataReflector.getCurrencySymbol()).isEqualTo("$");
      assertThat(localeDataReflector.getInternationalCurrencySymbol()).isEqualTo("USD");
    }

    assertThat(localeDataReflector.getNumberPattern()).isEqualTo("#,##0.###");
    assertThat(localeDataReflector.getIntegerPattern()).isEqualTo("#,##0");
    assertThat(localeDataReflector.getCurrencyPattern()).isEqualTo("¤#,##0.00;(¤#,##0.00)");
    assertThat(localeDataReflector.getPercentPattern()).isEqualTo("#,##0%");
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.O)
  public void shouldSupportLocaleEn_US_perMill() {
    LocaleData localeData = LocaleData.get(Locale.US);
    LocaleDataReflector localeDataReflector = reflector(LocaleDataReflector.class, localeData);
    assertThat(localeDataReflector.getPerMill()).isEqualTo('‰');
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void shouldSupportLocaleEn_US_perMillPostP() {
    LocaleData localeData = LocaleData.get(Locale.US);
    LocaleDataReflector localeDataReflector = reflector(LocaleDataReflector.class, localeData);
    assertThat(localeDataReflector.getPerMillString()).isEqualTo("‰");
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void shouldSupportLocaleEn_US_percentPost22() {
    LocaleData localeData = LocaleData.get(Locale.US);
    LocaleDataReflector localeDataReflector = reflector(LocaleDataReflector.class, localeData);
    assertThat(localeDataReflector.getPercentString()).isEqualTo("%");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldSupportLocaleEn_US_since_jelly_bean_mr1()
      throws NoSuchFieldException, IllegalAccessException {
    LocaleData localeData = LocaleData.get(Locale.US);
    LocaleDataReflector localeDataReflector = reflector(LocaleDataReflector.class, localeData);

    assertThat(localeData.tinyMonthNames)
        .isEqualTo(new String[] {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"});
    assertThat(localeData.tinyStandAloneMonthNames).isEqualTo(localeData.tinyMonthNames);
    assertThat(localeData.tinyWeekdayNames)
        .isEqualTo(new String[] {"", "S", "M", "T", "W", "T", "F", "S"});
    assertThat(localeData.tinyStandAloneWeekdayNames).isEqualTo(localeData.tinyWeekdayNames);

    if (getApiLevel() <= R) {
      assertThat(localeDataReflector.getYesterday()).isEqualTo("Yesterday");
    }
    assertThat(localeData.today).isEqualTo("Today");
    assertThat(localeData.tomorrow).isEqualTo("Tomorrow");
  }

  @Test
  @Config(minSdk = M)
  public void shouldSupportLocaleEn_US_since_m() {
    LocaleData localeData = LocaleData.get(Locale.US);

    assertThat(localeData.timeFormat_Hm).isEqualTo("HH:mm");
    assertThat(localeData.timeFormat_hm).isEqualTo("h:mm a");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldSupportLocaleEn_US_since_lollipop() {
    LocaleData localeData = LocaleData.get(Locale.US);
    LocaleDataReflector localeDataReflector = reflector(LocaleDataReflector.class, localeData);
    assertThat(localeDataReflector.getMinusSignString()).isEqualTo("-");
  }

  @Test
  public void shouldDefaultToTheDefaultLocale() {
    Locale.setDefault(Locale.US);
    LocaleData localeData = LocaleData.get(null);

    assertThat(localeData.amPm).isEqualTo(new String[] {"AM", "PM"});
  }

  /** Accessor interface for {@link LocaleData}'s internals. */
  @ForType(LocaleData.class)
  interface LocaleDataReflector {

    @Accessor("minusSign")
    char getMinusSign();

    @Accessor("percent")
    char getPercent();

    @Accessor("perMill")
    char getPerMill();

    // <= R
    @Accessor("yesterday")
    String getYesterday();

    // <= R
    @Accessor("currencySymbol")
    String getCurrencySymbol();

    // <= R
    @Accessor("internationalCurrencySymbol")
    String getInternationalCurrencySymbol();

    // <= S_V2
    @Accessor("fullTimeFormat")
    String getFullTimeFormat();

    // <= S_V2
    @Accessor("longTimeFormat")
    String getLongTimeFormat();

    // <= S_V2
    @Accessor("mediumTimeFormat")
    String getMediumTimeFormat();

    // <= S_V2
    @Accessor("shortTimeFormat")
    String getShortTimeFormat();

    // <= S_V2
    @Accessor("fullDateFormat")
    String getFullDateFormat();

    // <= S_V2
    @Accessor("longDateFormat")
    String getLongDateFormat();

    // <= S_V2
    @Accessor("mediumDateFormat")
    String getMediumDateFormat();

    // <= S_V2
    @Accessor("shortDateFormat")
    String getShortDateFormat();

    // <= S_V2
    @Accessor("decimalSeparator")
    char getDecimalSeparator();

    // <= S_V2
    @Accessor("groupingSeparator")
    char getGroupingSeparator();

    // <= S_V2
    @Accessor("patternSeparator")
    char getPatternSeparator();

    // <= S_V2
    @Accessor("percent")
    String getPercentString();

    // <= S_V2
    @Accessor("perMill")
    String getPerMillString();

    // <= S_V2
    @Accessor("monetarySeparator")
    char getMonetarySeparator();

    // <= S_V2
    @Accessor("minusSign")
    String getMinusSignString();

    // <= S_V2
    @Accessor("exponentSeparator")
    String getExponentSeparator();

    // <= S_V2
    @Accessor("infinity")
    String getInfinity();

    // <= S_V2
    @Accessor("NaN")
    String getNaN();

    // <= S_V2
    @Accessor("numberPattern")
    String getNumberPattern();

    // <= S_V2
    @Accessor("integerPattern")
    String getIntegerPattern();

    // <= S_V2
    @Accessor("currencyPattern")
    String getCurrencyPattern();

    // <= S_V2
    @Accessor("percentPattern")
    String getPercentPattern();
  }
}
