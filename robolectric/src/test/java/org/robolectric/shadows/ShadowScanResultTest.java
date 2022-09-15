package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.ScanResult;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowScanResultTest {

  @Test
  public void shouldConstruct() {
    ScanResult scanResult = ShadowScanResult.newInstance("SSID", "BSSID", "Caps", 11, 42);
    assertThat(scanResult.SSID).isEqualTo("SSID");
    assertThat(scanResult.BSSID).isEqualTo("BSSID");
    assertThat(scanResult.capabilities).isEqualTo("Caps");
    assertThat(scanResult.level).isEqualTo(11);
    assertThat(scanResult.frequency).isEqualTo(42);
  }
}
