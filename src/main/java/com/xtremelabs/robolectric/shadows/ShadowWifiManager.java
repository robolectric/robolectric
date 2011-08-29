package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class ShadowWifiManager {
    private boolean wifiEnabled = true;
    private static WifiInfo wifiInfo;

    @Implementation
    public boolean setWifiEnabled(boolean wifiEnabled) {
        this.wifiEnabled = wifiEnabled;
        return true;
    }

    @Implementation
    public boolean isWifiEnabled() {
        return wifiEnabled;
    }

    @Implementation
    public WifiInfo getConnectionInfo() {
        if (wifiInfo == null) {
            wifiInfo = Robolectric.newInstanceOf(WifiInfo.class);
        }
        return wifiInfo;
    }
}
