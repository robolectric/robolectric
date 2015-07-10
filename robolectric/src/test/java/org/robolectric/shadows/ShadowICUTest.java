package org.robolectric.shadows;

import android.os.Build;

import libcore.icu.ICU;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP })
public class ShadowICUTest {

  @Test
  public void getBestDateTimePattern_returnsReasonableValue() {
    assertThat(ICU.getBestDateTimePattern("hm", null)).isEqualTo("h:mm a");
  }
}