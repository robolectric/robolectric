package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.app.Application;
import android.os.SystemClock;
import android.text.format.DateUtils;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(AndroidJUnit4.class)
public class ShadowDateUtilsTest {

  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  @Config(maxSdk = LOLLIPOP_MR1)
  public void formatDateTime_withCurrentYear() {
    final long millisAtStartOfYear = getMillisAtStartOfYear();

    String actual =
        DateUtils.formatDateTime(context, millisAtStartOfYear, DateUtils.FORMAT_NUMERIC_DATE);
    assertThat(actual).isEqualTo("1/1");
  }

  @Test
  @Config(minSdk = M)
  public void formatDateTime_withCurrentYear_worksSinceM() {
    final long millisAtStartOfYear = getMillisAtStartOfYear();

    // starting with M, sometimes the year is there, sometimes it's missing, unless you specify
    // FORMAT_SHOW_YEAR
    String actual =
        DateUtils.formatDateTime(
            context,
            millisAtStartOfYear,
            DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE);
    final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    assertThat(actual).isEqualTo("1/1/" + currentYear);
  }

  @Test
  public void formatDateTime_withPastYear() {
    String actual =
        DateUtils.formatDateTime(context, 1420099200000L, DateUtils.FORMAT_NUMERIC_DATE);
    assertThat(actual).isEqualTo("1/1/2015");
  }

  @Test
  @LooperMode(LEGACY)
  public void isToday_shouldReturnFalseForNotToday() {
    long today = java.util.Calendar.getInstance().getTimeInMillis();
    SystemClock.setCurrentTimeMillis(today);

    assertThat(DateUtils.isToday(today)).isTrue();
    assertThat(DateUtils.isToday(today + (86400 * 1000) /* 24 hours */)).isFalse();
    assertThat(DateUtils.isToday(today + (86400 * 10000) /* 240 hours */)).isFalse();
  }

  private long getMillisAtStartOfYear() {
    Calendar calendar = Calendar.getInstance();
    final int currentYear = calendar.get(Calendar.YEAR);
    calendar.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
    calendar.set(currentYear, Calendar.JANUARY, 1, 0, 0, 0);

    return calendar.getTimeInMillis();
  }
}
