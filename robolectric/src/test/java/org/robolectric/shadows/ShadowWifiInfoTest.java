package org.robolectric.shadows;

import static android.content.Context.WIFI_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.net.InetAddress;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowWifiInfoTest {

  private WifiManager wifiManager;

  @Before
  public void setUp() {
    wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
  }

  @Test
  public void newInstance_shouldNotCrash() throws Exception {
    assertThat(ShadowWifiInfo.newInstance()).isNotNull();
  }

  @Test
  public void shouldReturnIpAddress() throws Exception {
    String ipAddress = "192.168.0.1";
    int expectedIpAddress = 16820416;

    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    shadowOf(wifiInfo).setInetAddress(InetAddress.getByName(ipAddress));
    assertThat(wifiInfo.getIpAddress()).isEqualTo(expectedIpAddress);
  }

  @Test
  public void shouldReturnMacAddress() {

    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

    shadowOf(wifiInfo).setMacAddress("mac address");

    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getMacAddress()).isEqualTo("mac address");
  }

  @Test
  public void shouldReturnSSID() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

    shadowOf(wifiInfo).setSSID("SSID");

    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getSSID()).contains("SSID");
  }

  @Test
  public void shouldReturnBSSID() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getBSSID()).isEqualTo(null);

    shadowOf(wifiInfo).setBSSID("BSSID");

    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getBSSID()).isEqualTo("BSSID");
  }

  @Test
  public void shouldReturnRssi() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

    shadowOf(wifiInfo).setRssi(10);

    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getRssi()).isEqualTo(10);
  }

  @Test
  public void shouldReturnLinkSpeed() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getLinkSpeed()).isEqualTo(-1);

    shadowOf(wifiInfo).setLinkSpeed(10);

    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getLinkSpeed()).isEqualTo(10);
  }

  @Test @Config(minSdk = LOLLIPOP)
  public void shouldReturnFrequency() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getFrequency()).isEqualTo(-1);

    shadowOf(wifiInfo).setFrequency(10);

    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getFrequency()).isEqualTo(10);
  }

  @Test
  public void shouldReturnNetworkId() {
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getNetworkId()).isEqualTo(-1);

    shadowOf(wifiInfo).setNetworkId(10);

    wifiInfo = wifiManager.getConnectionInfo();
    assertThat(wifiInfo.getNetworkId()).isEqualTo(10);
  }
}
