package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {

    private String ssid;
    private String macAddress;

    @Implementation
    public String getSSID() {
        return ssid;
    }

    public void setSSID(String ssid) {
        this.ssid = ssid;
    }

    @Implementation
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String newMacAddress) {
        macAddress = newMacAddress;
    }
}
