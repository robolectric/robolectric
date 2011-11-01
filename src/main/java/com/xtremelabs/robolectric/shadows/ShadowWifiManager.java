package com.xtremelabs.robolectric.shadows;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class ShadowWifiManager {
    private boolean accessWifiStatePermission = true;
    private boolean wifiEnabled = true;
    private WifiInfo wifiInfo;
    private List<ScanResult> scanResults;

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

    @Implementation
    public List<ScanResult> getScanResults() {
        return scanResults;
    }

    public void setAccessWifiStatePermission(boolean accessWifiStatePermission) {
        this.accessWifiStatePermission = accessWifiStatePermission;
    }

    private void checkAccessWifiStatePermission() {
        if (!accessWifiStatePermission) {
            throw new SecurityException();
        }
    }

    public void setScanResults(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }
}
