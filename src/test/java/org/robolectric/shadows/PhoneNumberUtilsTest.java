package org.robolectric.shadows;

import android.telephony.PhoneNumberUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PhoneNumberUtilsTest {
  @Test
  public void testFormatNumber() throws Exception {
    assertThat(PhoneNumberUtils.formatNumber("4155550780")).isEqualTo("415-555-0780");
  }

  @Test
  public void testIsEmergencyNumber() throws Exception {
    assertThat(PhoneNumberUtils.isEmergencyNumber("911")).isTrue();
    assertThat(PhoneNumberUtils.isEmergencyNumber("411")).isFalse();
  }

  @Test
  public void testStripSeparators() {
    assertThat(PhoneNumberUtils.stripSeparators("1-234-567-8901")).isEqualTo("12345678901");
  }
}
