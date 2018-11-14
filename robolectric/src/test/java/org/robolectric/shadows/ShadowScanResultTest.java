package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.net.wifi.ScanResult;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowScanResultTest {

  @Test
  public void shouldConstruct() throws Exception {
    ScanResult scanResult = ShadowScanResult.newInstance("SSID", "BSSID", "Caps", 11, 42);
    assertThat(scanResult.SSID).isEqualTo("SSID");
    assertThat(scanResult.BSSID).isEqualTo("BSSID");
    assertThat(scanResult.capabilities).isEqualTo("Caps");
    assertThat(scanResult.level).isEqualTo(11);
    assertThat(scanResult.frequency).isEqualTo(42);
    assertNotNull(shadowOf(scanResult).realObject);
  }
}
