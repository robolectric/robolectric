package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.WIFI_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.application;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class WifiInfoTest {

    @Test
    public void shouldReturnMacAddress() {
        WifiManager wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        shadowOf(wifiInfo).setMacAddress("mac address");

        wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        assertThat(wifiInfo.getMacAddress(), equalTo("mac address"));
    }
}
