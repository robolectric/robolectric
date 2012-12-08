package org.robolectric.shadows;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Pair;
import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class ShadowWifiManager {
    private static float sSignalLevelInPercent=1f;
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
        //if (wifiInfo == null) {
        //    wifiInfo = Robolectric.newInstanceOf(WifiInfo.class);
        //}
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

    @Implementation
    public WifiManager.WifiLock createWifiLock(int lockType, java.lang.String tag) {
        return Robolectric.newInstanceOf(WifiManager.WifiLock.class);
    }

    @Implementation
    public WifiManager.WifiLock createWifiLock(java.lang.String tag) {
        return createWifiLock(WifiManager.WIFI_MODE_FULL, tag);
    }
    
    @Implementation
    public static int calculateSignalLevel (int rssi, int numLevels)
    {
        return (int)(sSignalLevelInPercent*(numLevels-1));
    }
    
    public static void setSignalLevelInPercent(float level) {
        if (level < 0 || level > 1) {
            throw new IllegalArgumentException(
                    "level needs to be between 0 and 1");
        }
        sSignalLevelInPercent = level;
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


    @Implements(WifiManager.WifiLock.class)
    public static class ShadowWifiLock {
        private int refCount;
        private boolean refCounted = true;
        private boolean locked;
        public static final int MAX_ACTIVE_LOCKS = 50;

        @Implementation
        public synchronized void acquire() {
            if (refCounted) {
                if (++refCount >= MAX_ACTIVE_LOCKS) throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
            } else {
                locked = true;
            }
        }

        @Implementation
        public synchronized void release() {
            if (refCounted) {
                if (--refCount < 0) throw new RuntimeException("WifiLock under-locked");
            } else {
                locked = false;
            }
        }

        @Implementation
        public synchronized boolean isHeld() {
            return refCounted ? refCount > 0 : locked;
        }

        @Implementation
        public void setReferenceCounted(boolean refCounted) {
            this.refCounted = refCounted;
        }
    }
}
