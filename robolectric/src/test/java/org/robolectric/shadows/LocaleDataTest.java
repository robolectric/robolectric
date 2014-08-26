package org.robolectric.shadows;

import java.util.Locale;
import libcore.icu.LocaleData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import static org.fest.assertions.api.Assertions.*;

@RunWith(TestRunners.WithDefaults.class)
public class LocaleDataTest {

  @Test
  public void shouldSupportLocaleEn_US() throws Exception {
    LocaleData localeData = LocaleData.get(Locale.US);

    assertThat(localeData.amPm).isEqualTo(new String[]{"AM", "PM"});
    assertThat(localeData.eras).isEqualTo(new String[]{"BC", "AD"});

    assertThat(localeData.firstDayOfWeek).isEqualTo(1);
    assertThat(localeData.minimalDaysInFirstWeek).isEqualTo(1);

    assertThat(localeData.longMonthNames).isEqualTo(new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"});
    assertThat(localeData.shortMonthNames).isEqualTo(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
    assertThat(localeData.tinyMonthNames).isEqualTo(new String[]{"J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"});

    assertThat(localeData.longStandAloneMonthNames).isEqualTo(localeData.longMonthNames);
    assertThat(localeData.shortStandAloneMonthNames).isEqualTo(localeData.shortMonthNames);
    assertThat(localeData.tinyStandAloneMonthNames).isEqualTo(localeData.tinyMonthNames);

    assertThat(localeData.longWeekdayNames).isEqualTo(new String[]{"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"});
    assertThat(localeData.shortWeekdayNames).isEqualTo(new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
    assertThat(localeData.tinyWeekdayNames).isEqualTo(new String[]{"", "S", "M", "T", "W", "T", "F", "S"});

    assertThat(localeData.longStandAloneWeekdayNames).isEqualTo(localeData.longWeekdayNames);
    assertThat(localeData.shortStandAloneWeekdayNames).isEqualTo(localeData.shortWeekdayNames);
    assertThat(localeData.tinyStandAloneWeekdayNames).isEqualTo(localeData.tinyWeekdayNames);

    assertThat(localeData.yesterday).isEqualTo("Yesterday");
    assertThat(localeData.today).isEqualTo("Today");
    assertThat(localeData.tomorrow).isEqualTo("Tomorrow");

    assertThat(localeData.fullTimeFormat).isEqualTo("h:mm:ss a zzzz");
    assertThat(localeData.longTimeFormat).isEqualTo("h:mm:ss a z");
    assertThat(localeData.mediumTimeFormat).isEqualTo("h:mm:ss a");
    assertThat(localeData.shortTimeFormat).isEqualTo("h:mm a");
    assertThat(localeData.timeFormat12).isEqualTo("h:mm a");
    assertThat(localeData.timeFormat24).isEqualTo("HH:mm");

    assertThat(localeData.fullDateFormat).isEqualTo("EEEE, MMMM d, y");
    assertThat(localeData.longDateFormat).isEqualTo("MMMM d, y");
    assertThat(localeData.mediumDateFormat).isEqualTo("MMM d, y");
    assertThat(localeData.shortDateFormat).isEqualTo("M/d/yy");

    assertThat(localeData.zeroDigit).isEqualTo('0');
    assertThat(localeData.decimalSeparator).isEqualTo('.');
    assertThat(localeData.groupingSeparator).isEqualTo(',');
    assertThat(localeData.patternSeparator).isEqualTo(';');
    assertThat(localeData.percent).isEqualTo('%');
    assertThat(localeData.perMill).isEqualTo('‰');
    assertThat(localeData.monetarySeparator).isEqualTo('.');
    assertThat(localeData.minusSign).isEqualTo('-');

    assertThat(localeData.exponentSeparator).isEqualTo("E");
    assertThat(localeData.infinity).isEqualTo("∞");
    assertThat(localeData.NaN).isEqualTo("NaN");

    assertThat(localeData.currencySymbol).isEqualTo("$");
    assertThat(localeData.internationalCurrencySymbol).isEqualTo("USD");

    assertThat(localeData.numberPattern).isEqualTo("#,##0.###");
    assertThat(localeData.integerPattern).isEqualTo("#,##0");
    assertThat(localeData.currencyPattern).isEqualTo("¤#,##0.00;(¤#,##0.00)");
    assertThat(localeData.percentPattern).isEqualTo("#,##0%");
  }

  @Test
  public void shouldDefaultToTheDefaultLocale() throws Exception {
    Locale.setDefault(Locale.US);
    LocaleData localeData = LocaleData.get(null);

    assertThat(localeData.amPm).isEqualTo(new String[]{"AM", "PM"});
  }

  @Test
  public void shouldPassThroughOtherLocales() throws Exception {
    LocaleData localeData = LocaleData.get(Locale.FRANCE);

    assertThat(localeData.amPm).isNull();
  }
}
