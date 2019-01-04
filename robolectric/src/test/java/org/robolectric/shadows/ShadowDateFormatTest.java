package org.robolectric.shadows;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getLongDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
public class ShadowDateFormatTest {

  private Application context;

  @Before
  public void setUp() throws Exception {
    context = RuntimeEnvironment.application;
  }

  @Test
  public void getTimeFormat_returnsATimeFormat() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.HOUR, 7);
    cal.set(Calendar.MINUTE, 48);
    cal.set(Calendar.SECOND, 3);
    Date date = cal.getTime();
    assertThat(getTimeFormat(context).format(date)).isEqualTo("07:48:03");
  }

  @Test
  public void getDateFormat_returnsADateFormat_January() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 12);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();
    assertThat(getDateFormat(context).format(date)).isEqualTo("Jan-12-1970");
  }

  @Test
  public void getDateFormat_returnsADateFormat_December() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 31);
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();
    assertThat(getDateFormat(context).format(date)).isEqualTo("Dec-31-1970");
  }

  @Test
  public void getLongDateFormat_returnsADateFormat_January() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 12);
    cal.set(Calendar.MONTH, Calendar.JANUARY);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();

    assertThat(getLongDateFormat(context).format(date)).isEqualTo("January 12, 1970");
  }

  @Test
  public void getLongDateFormat_returnsADateFormat_December() {
    Calendar cal = Calendar.getInstance();
    cal.clear();
    cal.set(Calendar.DATE, 31);
    cal.set(Calendar.MONTH, Calendar.DECEMBER);
    cal.set(Calendar.YEAR, 1970);
    Date date = cal.getTime();
    assertThat(getLongDateFormat(context).format(date)).isEqualTo("December 31, 1970");
  }

}
