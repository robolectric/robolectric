package android.telephony;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that Robolectric's android.telephony.PhoneNumberUtils support is consistent with device.
 */
@RunWith(AndroidJUnit4.class)
public class PhoneNumberUtilsTest {
  // This test requires the libphonenumber metadata in the Android SDK.
  @Test
  public void formatNumber() {
    String number1 = "+16501230003";
    assertThat(PhoneNumberUtils.formatNumber(number1, "us")).isEqualTo("+1 650-123-0003");
  }
}
