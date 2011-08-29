package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.WIFI_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.application;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class WifiManagerTest {

    private WifiManager wifiManager;

    @Before
    public void setUp() throws Exception {
        wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
    }

    @Test
    public void shouldReturnWifiInfo() {
        assertEquals(wifiManager.getConnectionInfo().getClass(), WifiInfo.class);
    }

    @Test(expected = SecurityException.class)
    public void setWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        ShadowWifiManager.setAccessWifiStatePermissionGranted(false);
        wifiManager.setWifiEnabled(true);
    }

    @Test(expected = SecurityException.class)
    public void isWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        ShadowWifiManager.setAccessWifiStatePermissionGranted(false);
        wifiManager.isWifiEnabled();
    }

    @Test(expected = SecurityException.class)
    public void getConnectionInfo_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        ShadowWifiManager.setAccessWifiStatePermissionGranted(false);
        wifiManager.getConnectionInfo();
    }

}
