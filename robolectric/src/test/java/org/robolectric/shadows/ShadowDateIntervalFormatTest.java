package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;

import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.text.format.DateUtils;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import libcore.icu.DateIntervalFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = M)
public class ShadowDateIntervalFormatTest {
  @Test
  public void testDateInterval_FormatDateRange() throws ParseException {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2013);
    calendar.set(Calendar.MONTH, Calendar.JANUARY);
    calendar.set(Calendar.DAY_OF_MONTH, 20);

    long timeInMillis = calendar.getTimeInMillis();
    String actual = DateIntervalFormat.formatDateRange(ULocale.getDefault(), TimeZone.getDefault(), timeInMillis, timeInMillis, DateUtils.FORMAT_NUMERIC_DATE);

    DateFormat format = new SimpleDateFormat("MM/dd/yyyy", ULocale.getDefault());
    Date date = format.parse(actual);

    assertThat(date).isNotNull();
  }
}
