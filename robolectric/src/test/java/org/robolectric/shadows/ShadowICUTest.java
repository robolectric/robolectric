package org.robolectric.shadows;

import libcore.icu.ICU;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowICUTest {

  @Test
  public void getBestDateTimePattern_returnsReasonableValue() {
    assertThat(ICU.getBestDateTimePattern("hm", null)).isEqualTo("h:mm a");
  }
}