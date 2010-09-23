package com.xtremelabs.droidsugar.fakes;

import android.net.wifi.WifiManager;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class FakeWifiManager {
    private boolean wifiEnabled = true;

    public boolean setWifiEnabled(boolean wifiEnabled) {
        this.wifiEnabled = wifiEnabled;
        return true;
    }

    public boolean isWifiEnabled() {
        return wifiEnabled;
    }

}
