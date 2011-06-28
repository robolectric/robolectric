package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class WifiInfoTest {

    private WifiInfo wifiInfo;

    @Before
    public void setUp() throws Exception {
        wifiInfo = Robolectric.newInstanceOf(WifiInfo.class);
    }

    @Test
    public void getMacAddress_shouldBeMockable() {
        String bigMac = "a8:2e:33:f3:29:81";
        shadowOf(wifiInfo).setMacAddress(bigMac);
        assertEquals(bigMac, wifiInfo.getMacAddress());
    }
}
