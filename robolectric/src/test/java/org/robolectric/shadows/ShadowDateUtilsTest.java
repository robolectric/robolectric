package org.robolectric.shadows;

import android.text.format.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowDateUtilsTest {

  @Test
  public void formatDateTime_worksOnLollipop() {
    String actual = DateUtils.formatDateTime(RuntimeEnvironment.application, 1420099200000L, DateUtils.FORMAT_NUMERIC_DATE);
    assertThat(actual).isEqualTo("1/1");
  }

  @Test @Config(emulateSdk = 18)
  public void formatDateTime_worksOnJellybean() {
    String actual = DateUtils.formatDateTime(RuntimeEnvironment.application, 1420099200000L, DateUtils.FORMAT_NUMERIC_DATE);
    assertThat(actual).isEqualTo("1/1/2015");
  }

  @Test @Config(emulateSdk = 18)
  public void isToday_shouldReturnFalseForNotToday() {
    long today = java.util.Calendar.getInstance().getTimeInMillis();
    ShadowSystemClock.setCurrentTimeMillis(today);

    assertThat(DateUtils.isToday(today)).isTrue();
    assertThat(DateUtils.isToday(today + (86400 * 1000)  /* 24 hours */)).isFalse();
    assertThat(DateUtils.isToday(today + (86400 * 10000) /* 240 hours */)).isFalse();
  }
}
