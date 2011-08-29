package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.WIFI_SERVICE;
import static com.xtremelabs.robolectric.Robolectric.application;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class WifiManagerTest {

    private WifiManager wifiManager;
    private ShadowWifiManager shadowWifiManager;

    @Before
    public void setUp() throws Exception {
        wifiManager = (WifiManager) application.getSystemService(WIFI_SERVICE);
        shadowWifiManager = shadowOf(wifiManager);
    }

    @Test
    public void shouldReturnWifiInfo() {
        assertEquals(wifiManager.getConnectionInfo().getClass(), WifiInfo.class);
    }

    @Test(expected = SecurityException.class)
    public void setWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        shadowWifiManager.setAccessWifiStatePermission(false);
        wifiManager.setWifiEnabled(true);
    }

    @Test(expected = SecurityException.class)
    public void isWifiEnabled_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        shadowWifiManager.setAccessWifiStatePermission(false);
        wifiManager.isWifiEnabled();
    }

    @Test(expected = SecurityException.class)
    public void getConnectionInfo_shouldThrowSecurityExceptionWhenAccessWifiStatePermissionNotGranted() throws Exception {
        shadowWifiManager.setAccessWifiStatePermission(false);
        wifiManager.getConnectionInfo();
    }

}
