package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@RunWith(WithTestDefaultsRunner.class)
public class WifiManagerTest {

    private WifiManager wifiManager;

    @Before
    public void setUp() throws Exception {
        wifiManager = Robolectric.newInstanceOf(WifiManager.class);
    }

    @Test
    public void getWifiInfo_shouldReturnSomething() {
        assertNotNull(wifiManager.getConnectionInfo());
    }

    @Test
    public void getWifiInfo_shouldBeMockable() {
        WifiInfo wifiInfo = Robolectric.newInstanceOf(WifiInfo.class);
        shadowOf(wifiManager).setConnectionInfo(wifiInfo);
        assertSame(wifiInfo, wifiManager.getConnectionInfo());
    }


}
