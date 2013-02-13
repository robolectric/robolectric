package org.robolectric.shadows;

import android.net.wifi.WifiInfo;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(WifiInfo.class)
public class ShadowWifiInfo {
    public static void __staticInitializer__() {
    }

    private String macAddress;

    @Implementation
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String newMacAddress) {
        macAddress = newMacAddress;
    }
}
