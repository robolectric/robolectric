package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import libcore.icu.LocaleData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowLocaleDataTest {

  @Test
  public void shouldSupportLocaleEn_US() throws NoSuchFieldException, IllegalAccessException {
    LocaleData localeData = LocaleData.get(Locale.US);

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

    assertThat(localeData.fullTimeFormat).isEqualTo("h:mm:ss a zzzz");
    assertThat(localeData.longTimeFormat).isEqualTo("h:mm:ss a z");
    assertThat(localeData.mediumTimeFormat).isEqualTo("h:mm:ss a");
    assertThat(localeData.shortTimeFormat).isEqualTo("h:mm a");

    assertThat(localeData.fullDateFormat).isEqualTo("EEEE, MMMM d, y");
    assertThat(localeData.longDateFormat).isEqualTo("MMMM d, y");
    assertThat(localeData.mediumDateFormat).isEqualTo("MMM d, y");
    assertThat(localeData.shortDateFormat).isEqualTo("M/d/yy");

    assertThat(localeData.zeroDigit).isEqualTo('0');
    assertThat(localeData.decimalSeparator).isEqualTo('.');
    assertThat(localeData.groupingSeparator).isEqualTo(',');
    assertThat(localeData.patternSeparator).isEqualTo(';');

    assertThat(localeData.monetarySeparator).isEqualTo('.');

    assertThat(localeData.exponentSeparator).isEqualTo("E");
    assertThat(localeData.infinity).isEqualTo("∞");
    assertThat(localeData.NaN).isEqualTo("NaN");

    if (getApiLevel() <= R) {
      assertThat((String) ReflectionHelpers.getField(localeData, "currencySymbol")).isEqualTo("$");
      assertThat((String) ReflectionHelpers.getField(localeData, "internationalCurrencySymbol"))
          .isEqualTo("USD");
    }

    assertThat(localeData.numberPattern).isEqualTo("#,##0.###");
    assertThat(localeData.integerPattern).isEqualTo("#,##0");
    assertThat(localeData.currencyPattern).isEqualTo("¤#,##0.00;(¤#,##0.00)");
    assertThat(localeData.percentPattern).isEqualTo("#,##0%");
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.O)
  public void shouldSupportLocaleEn_US_perMill() {
    LocaleData localeData = LocaleData.get(Locale.US);
    char perMillValue = ReflectionHelpers.getField(localeData, "perMill");
    assertThat(perMillValue).isEqualTo('‰');
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void shouldSupportLocaleEn_US_perMillPostP() {
    LocaleData localeData = LocaleData.get(Locale.US);
    assertThat(localeData.perMill).isEqualTo("‰");
  }

  @Test
  @Config(minSdk = LOLLIPOP_MR1)
  public void shouldSupportLocaleEn_US_percentPost22() {
    LocaleData localeData = LocaleData.get(Locale.US);
    assertThat(localeData.percent).isEqualTo("%");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldSupportLocaleEn_US_since_jelly_bean_mr1()
      throws NoSuchFieldException, IllegalAccessException {
    LocaleData localeData = LocaleData.get(Locale.US);

    assertThat(localeData.tinyMonthNames)
        .isEqualTo(new String[] {"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"});
    assertThat(localeData.tinyStandAloneMonthNames).isEqualTo(localeData.tinyMonthNames);
    assertThat(localeData.tinyWeekdayNames)
        .isEqualTo(new String[] {"", "S", "M", "T", "W", "T", "F", "S"});
    assertThat(localeData.tinyStandAloneWeekdayNames).isEqualTo(localeData.tinyWeekdayNames);

    if (getApiLevel() <= R) {
      assertThat((String) ReflectionHelpers.getField(localeData, "yesterday"))
          .isEqualTo("Yesterday");
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

    assertThat(localeData.minusSign).isEqualTo("-");
  }

  @Test
  public void shouldDefaultToTheDefaultLocale() {
    Locale.setDefault(Locale.US);
    LocaleData localeData = LocaleData.get(null);

    assertThat(localeData.amPm).isEqualTo(new String[] {"AM", "PM"});
  }
}
