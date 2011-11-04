package com.xtremelabs.robolectric.shadows;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Pair;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class ShadowWifiManager {
    private boolean accessWifiStatePermission = true;
    private boolean wifiEnabled = true;
    private WifiInfo wifiInfo;
    private List<ScanResult> scanResults;
    private Map<Integer, WifiConfiguration> networkIdToConfiguredNetworks = new LinkedHashMap<Integer, WifiConfiguration>();
    public boolean wasSaved;
    private Pair<Integer, Boolean> lastEnabledNetwork;

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

    @Implementation
    public List<WifiConfiguration> getConfiguredNetworks() {
        final ArrayList<WifiConfiguration> wifiConfigurations = new ArrayList<WifiConfiguration>();
        for (WifiConfiguration wifiConfiguration : networkIdToConfiguredNetworks.values()) {
            wifiConfigurations.add(wifiConfiguration);
        }
        return wifiConfigurations;
    }

    @Implementation
    public int addNetwork(WifiConfiguration config) {
        int networkId = networkIdToConfiguredNetworks.size();
        config.networkId = -1;
        networkIdToConfiguredNetworks.put(networkId, makeCopy(config, networkId));
        return networkId;
    }

    private WifiConfiguration makeCopy(WifiConfiguration config, int networkId) {
        WifiConfiguration copy = shadowOf(config).copy();
        copy.networkId = networkId;
        return copy;
    }


    @Implementation
    public int updateNetwork(WifiConfiguration config) {
        if (config == null || config.networkId < 0) {
            return -1;
        }
        networkIdToConfiguredNetworks.put(config.networkId, makeCopy(config, config.networkId));
        return config.networkId;
    }

    @Implementation
    public boolean saveConfiguration() {
        wasSaved = true;
        return true;
    }

    @Implementation
    public boolean enableNetwork(int netId, boolean disableOthers) {
        lastEnabledNetwork = new Pair<Integer, Boolean>(netId, disableOthers);
        return true;
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

    public Pair<Integer, Boolean> getLastEnabledNetwork() {
        return lastEnabledNetwork;
    }
}
