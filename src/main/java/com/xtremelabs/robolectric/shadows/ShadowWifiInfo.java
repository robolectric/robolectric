package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiInfo.class)
public class ShadowWifiInfo {

    private String macAddress;

    @Implementation
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
