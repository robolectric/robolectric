package org.robolectric.shadows;

import android.os.Build;
import android.text.format.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowDateUtilsTest {

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.KITKAT,
      Build.VERSION_CODES.LOLLIPOP })
  public void formatDateTime_worksSinceKitKat() {
    String actual = DateUtils.formatDateTime(RuntimeEnvironment.application, 1420099200000L, DateUtils.FORMAT_NUMERIC_DATE);
    assertThat(actual).isEqualTo("1/1");
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.JELLY_BEAN,
      Build.VERSION_CODES.JELLY_BEAN_MR1,
      Build.VERSION_CODES.JELLY_BEAN_MR2 })
  public void formatDateTime_worksPreKitKat() {
    String actual = DateUtils.formatDateTime(RuntimeEnvironment.application, 1420099200000L, DateUtils.FORMAT_NUMERIC_DATE);
    assertThat(actual).isEqualTo("1/1/2015");
  }

  @Test
  public void isToday_shouldReturnFalseForNotToday() {
    long today = java.util.Calendar.getInstance().getTimeInMillis();
    ShadowSystemClock.setCurrentTimeMillis(today);

    assertThat(DateUtils.isToday(today)).isTrue();
    assertThat(DateUtils.isToday(today + (86400 * 1000)  /* 24 hours */)).isFalse();
    assertThat(DateUtils.isToday(today + (86400 * 10000) /* 240 hours */)).isFalse();
  }
}
