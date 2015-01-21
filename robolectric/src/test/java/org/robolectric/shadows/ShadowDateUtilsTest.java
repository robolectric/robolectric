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
}
