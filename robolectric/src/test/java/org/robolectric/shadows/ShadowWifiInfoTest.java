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
    assertThat(wifiInfo.getMacAddress())
        .isEqualTo("02:00:00:00:00:00"); // WifiInfo.DEFAULT_MAC_ADDRESS

    shadowOf(wifiInfo).setMacAddress("mac address");

    wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getMacAddress()).isEqualTo("mac address");
  }

  @Test
  public void shouldReturnSSID() {
    WifiManager wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getSSID()).isEqualTo("<unknown ssid>"); // WifiSsid.NONE

    shadowOf(wifiInfo).setSSID("SSID");

    wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getSSID()).isEqualTo("SSID");
  }

  @Test
  public void shouldReturnBSSID() {
    WifiManager wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getBSSID()).isEqualTo(null);

    shadowOf(wifiInfo).setBSSID("BSSID");

    wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getBSSID()).isEqualTo("BSSID");
  }

  @Test
  public void shouldReturnRssi() {
    WifiManager wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getRssi()).isEqualTo(-127); // WifiInfo.INVALID_RSSI

    shadowOf(wifiInfo).setRssi(10);

    wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getRssi()).isEqualTo(10);
  }
}
