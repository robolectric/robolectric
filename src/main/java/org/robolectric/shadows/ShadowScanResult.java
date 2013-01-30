package com.xtremelabs.robolectric.shadows;

import android.net.wifi.ScanResult;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(ScanResult.class)
public class ShadowScanResult {

    @RealObject ScanResult realObject;

    public static ScanResult newInstance(String SSID, String BSSID, String caps, int level, int frequency) {
        ScanResult scanResult = Robolectric.newInstanceOf(ScanResult.class);
        scanResult.SSID = SSID;
        scanResult.BSSID = BSSID;
        scanResult.capabilities = caps;
        scanResult.level = level;
        scanResult.frequency = frequency;
        return scanResult;
    }

    @Override @Implementation
    public String toString() {
        return new StringBuffer()
                .append("SSID: ").append(valueOrNone(realObject.SSID))
                .append(", BSSID: ").append(valueOrNone(realObject.BSSID))
                .append(", capabilities: ").append(valueOrNone(realObject.capabilities))
                .append(", level: ").append(realObject.level)
                .append(", frequency: ").append(realObject.frequency)
                .toString();
    }

    private String valueOrNone(String value) {
        return value == null ? "<none>" : value;
    }
}

