package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiConfiguration;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
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

        assertThat(copy.networkId, equalTo(1));
        assertThat(copy.SSID, equalTo("SSID"));
        assertThat(copy.BSSID, equalTo("BSSID"));
        assertThat(copy.preSharedKey, equalTo("preSharedKey"));
        assertThat(copy.status, equalTo(666));
        assertThat(copy.wepTxKeyIndex, equalTo(777));
        assertThat(copy.priority, equalTo(2));
        assertThat(copy.hiddenSSID, equalTo(true));
        assertThat(copy.allowedKeyManagement.get(1), equalTo(true));
        assertThat(copy.allowedProtocols.get(2), equalTo(true));
        assertThat(copy.allowedAuthAlgorithms.get(3), equalTo(true));
        assertThat(copy.allowedPairwiseCiphers.get(4), equalTo(true));
        assertThat(copy.allowedGroupCiphers.get(5), equalTo(true));
        assertThat(copy.wepKeys[0], equalTo("0"));
        assertThat(copy.wepKeys[1], equalTo("1"));
        assertThat(copy.wepKeys[2], equalTo("2"));
        assertThat(copy.wepKeys[3], equalTo("3"));
    }
}
