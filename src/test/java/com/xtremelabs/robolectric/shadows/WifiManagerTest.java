package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.WIFI_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.application;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class WifiManagerTest {

    @Test
    public void shouldReturnWifiInfo() {
        WifiManager wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
        assertEquals(wifiManager.getConnectionInfo().getClass(), WifiInfo.class);
    }

}
