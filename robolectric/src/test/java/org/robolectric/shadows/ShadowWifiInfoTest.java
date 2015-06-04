package org.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static android.content.Context.WIFI_SERVICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowWifiInfoTest {

  @Test
  public void shouldReturnMacAddress() {
    WifiManager wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    shadowOf(wifiInfo).setMacAddress("mac address");

    wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getMacAddress()).isEqualTo("mac address");
  }
}
