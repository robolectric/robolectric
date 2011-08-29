package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class ShadowWifiManager {
    private boolean accessWifiStatePermission = true;
    private boolean wifiEnabled = true;
    private WifiInfo wifiInfo;

    @Implementation
    public boolean setWifiEnabled(boolean wifiEnabled) {
        checkAccessWifiStatePermission();
        this.wifiEnabled = wifiEnabled;
        return true;
    }

    @Implementation
    public boolean isWifiEnabled() {
        checkAccessWifiStatePermission();
        return wifiEnabled;
    }

    @Implementation
    public WifiInfo getConnectionInfo() {
        checkAccessWifiStatePermission();
        if (wifiInfo == null) {
            wifiInfo = Robolectric.newInstanceOf(WifiInfo.class);
        }
        return wifiInfo;
    }

    public void setAccessWifiStatePermission(boolean accessWifiStatePermission) {
        this.accessWifiStatePermission = accessWifiStatePermission;
    }

    private void checkAccessWifiStatePermission() {
        if (!accessWifiStatePermission) {
            throw new SecurityException();
        }
    }
}
