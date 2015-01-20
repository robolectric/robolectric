// Copyright 2015 Google Inc. All Rights Reserved.

package org.robolectric.shadows;

import android.text.format.DateUtils;
import libcore.icu.DateIntervalFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowDateIntervalFormatTest {

  @Test
  public void testDateInterval_FormatDateRange() {

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2013);
    calendar.set(Calendar.MONTH, Calendar.JANUARY);
    calendar.set(Calendar.DAY_OF_MONTH, 20);

    long timeInMillis = calendar.getTimeInMillis();
    String actual = DateIntervalFormat.formatDateRange(Locale.getDefault(), TimeZone.getDefault(), timeInMillis, timeInMillis, DateUtils.FORMAT_NUMERIC_DATE);

    assertEquals("1/20/2013", actual);
  }
}
