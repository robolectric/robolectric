package org.robolectric.shadows;

import android.net.wifi.WifiConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class WifiConfigurationTest {
  @Test
  public void shouldSetTheBitSetsAndWepKeyArrays() throws Exception {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    assertNotNull(wifiConfiguration.allowedAuthAlgorithms);
  }

  @Test
  public void shouldCopy() throws Exception {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();

    wifiConfiguration.networkId = 1;
    wifiConfiguration.SSID = "SSID";
    wifiConfiguration.BSSID = "BSSID";
    wifiConfiguration.preSharedKey = "preSharedKey";
    wifiConfiguration.status = 666;
    wifiConfiguration.wepTxKeyIndex = 777;
    wifiConfiguration.priority = 2;
    wifiConfiguration.hiddenSSID = true;
    wifiConfiguration.allowedKeyManagement.set(1);
    wifiConfiguration.allowedProtocols.set(2);
    wifiConfiguration.allowedAuthAlgorithms.set(3);
    wifiConfiguration.allowedPairwiseCiphers.set(4);
    wifiConfiguration.allowedGroupCiphers.set(5);
    wifiConfiguration.wepKeys[0] = "0";
    wifiConfiguration.wepKeys[1] = "1";
    wifiConfiguration.wepKeys[2] = "2";
    wifiConfiguration.wepKeys[3] = "3";

    WifiConfiguration copy = shadowOf(wifiConfiguration).copy();

    assertThat(copy.networkId).isEqualTo(1);
    assertThat(copy.SSID).isEqualTo("SSID");
    assertThat(copy.BSSID).isEqualTo("BSSID");
    assertThat(copy.preSharedKey).isEqualTo("preSharedKey");
    assertThat(copy.status).isEqualTo(666);
    assertThat(copy.wepTxKeyIndex).isEqualTo(777);
    assertThat(copy.priority).isEqualTo(2);
    assertThat(copy.hiddenSSID).isTrue();
    assertThat(copy.allowedKeyManagement.get(1)).isTrue();
    assertThat(copy.allowedProtocols.get(2)).isTrue();
    assertThat(copy.allowedAuthAlgorithms.get(3)).isTrue();
    assertThat(copy.allowedPairwiseCiphers.get(4)).isTrue();
    assertThat(copy.allowedGroupCiphers.get(5)).isTrue();
    assertThat(copy.wepKeys[0]).isEqualTo("0");
    assertThat(copy.wepKeys[1]).isEqualTo("1");
    assertThat(copy.wepKeys[2]).isEqualTo("2");
    assertThat(copy.wepKeys[3]).isEqualTo("3");
  }
}
