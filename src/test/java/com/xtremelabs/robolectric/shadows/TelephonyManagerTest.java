package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class TelephonyManagerTest {
    @Test
    public void shouldGiveDeviceId() {
        String testId = "TESTING123";
        ShadowTelephonyManager.setDeviceId(testId);
        TelephonyManager telephonyManager = (TelephonyManager) Robolectric.application.getSystemService(Context.TELEPHONY_SERVICE);
        assertEquals(testId, telephonyManager.getDeviceId());
    }

}
