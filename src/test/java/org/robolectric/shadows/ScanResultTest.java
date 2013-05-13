package org.robolectric.shadows;

import android.net.wifi.ScanResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ScanResultTest {

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
