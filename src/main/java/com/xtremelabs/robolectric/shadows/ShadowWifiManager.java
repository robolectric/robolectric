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
    private WifiInfo connectionInfo;

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
        if (connectionInfo != null) return connectionInfo;
        return Robolectric.newInstanceOf(WifiInfo.class);
    }

    public void setConnectionInfo(WifiInfo wifiInfo) {
        this.connectionInfo = wifiInfo;
    }
}
