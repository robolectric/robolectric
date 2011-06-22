package com.xtremelabs.robolectric.shadows;

import android.telephony.TelephonyManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(TelephonyManager.class)
public class ShadowTelephonyManager {
    private static String deviceId;

    @Implementation
    public String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String newDeviceId) {
        deviceId = newDeviceId;
    }
}
