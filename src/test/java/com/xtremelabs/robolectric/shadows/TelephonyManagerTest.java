package com.xtremelabs.robolectric.shadows;

import android.telephony.TelephonyManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class TelephonyManagerTest {

    private TelephonyManager telephonyManager;

    @Before
    public void setUp() throws Exception {
        telephonyManager = Robolectric.newInstanceOf(TelephonyManager.class);
    }

    @Test
    public void getDeviceId_shouldBeMockable() {
        String deviceId = "1234";
        shadowOf(telephonyManager).setDeviceId(deviceId);
        assertEquals(deviceId, telephonyManager.getDeviceId());
    }
}
