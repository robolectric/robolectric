package com.xtremelabs.robolectric.shadows;

import android.telephony.TelephonyManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TelephonyManager.class)
public class ShadowTelephonyManager {

    private String deviceId;

    @Implementation
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
