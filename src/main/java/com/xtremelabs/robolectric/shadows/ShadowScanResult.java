package com.xtremelabs.robolectric.shadows;

import android.net.wifi.ScanResult;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ScanResult.class)
public class ShadowScanResult {

    public static ScanResult newInstance(String SSID, String BSSID, String caps, int level, int frequency) {
        ScanResult scanResult = Robolectric.newInstanceOf(ScanResult.class);
        scanResult.SSID = SSID;
        scanResult.BSSID = BSSID;
        scanResult.capabilities = caps;
        scanResult.level = level;
        scanResult.frequency = frequency;
        return scanResult;
    }



}

