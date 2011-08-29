package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class ShadowWifiManager {
    private static boolean accessWifiStatePermissionGranted = true;
    private boolean wifiEnabled = true;
    private static WifiInfo wifiInfo;

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

    public static void setAccessWifiStatePermissionGranted(boolean b) {
        accessWifiStatePermissionGranted = b;
    }

    private void checkAccessWifiStatePermission() {
        if (!accessWifiStatePermissionGranted) {
            throw new SecurityException();
        }
    }
}
