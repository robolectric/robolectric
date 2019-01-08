package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;

import android.text.format.DateFormat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowDateFormatTest {

  @Test
  public void getTimeFormat_returnsATimeFormat() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.HOUR, 7);
    cal.set(Calendar.MINUTE, 48);
    cal.set(Calendar.SECOND, 3);
    Date date = cal.getTime();
    assertEquals("07:48:03", DateFormat.getTimeFormat(null).format(date));
  }

  @Test
  public void getDateFormat_returnsADateFormat_January() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 12);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();
    assertEquals("Jan-12-1970", DateFormat.getDateFormat(null).format(date));
  }

  @Test
  public void getDateFormat_returnsADateFormat_December() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 31);
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();
    assertEquals("Dec-31-1970", DateFormat.getDateFormat(null).format(date));
  }

  @Test
  public void getLongDateFormat_returnsADateFormat_January() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 12);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();
    assertEquals("January 12, 1970", DateFormat.getLongDateFormat(null).format(date));
  }

  @Test
  public void getLongDateFormat_returnsADateFormat_December() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 31);
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();
    assertEquals("December 31, 1970", DateFormat.getLongDateFormat(null).format(date));
  }

}
