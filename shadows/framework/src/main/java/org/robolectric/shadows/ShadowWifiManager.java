package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(WifiManager.class)
public class ShadowWifiManager {
  private static float sSignalLevelInPercent = 1f;
  private boolean accessWifiStatePermission = true;
  private boolean wifiEnabled = true;
  private boolean wasSaved = false;
  private WifiInfo wifiInfo;
  private List<ScanResult> scanResults;
  private final Map<Integer, WifiConfiguration> networkIdToConfiguredNetworks = new LinkedHashMap<>();
  private Pair<Integer, Boolean> lastEnabledNetwork;
  private DhcpInfo dhcpInfo;
  private boolean isScanAlwaysAvailable = true;

  @Implementation
  protected boolean setWifiEnabled(boolean wifiEnabled) {
    checkAccessWifiStatePermission();
    this.wifiEnabled = wifiEnabled;
    return true;
  }

  @Implementation
  protected boolean isWifiEnabled() {
    checkAccessWifiStatePermission();
    return wifiEnabled;
  }

  @Implementation
  protected int getWifiState() {
    if (isWifiEnabled()) {
      return WifiManager.WIFI_STATE_ENABLED;
    } else {
      return WifiManager.WIFI_STATE_DISABLED;
    }
  }

  @Implementation
  protected WifiInfo getConnectionInfo() {
    checkAccessWifiStatePermission();
    if (wifiInfo == null) {
      wifiInfo = ReflectionHelpers.callConstructor(WifiInfo.class);
    }
    return wifiInfo;
  }

  /**
   * Sets the connection info as the provided {@link WifiInfo}.
   */
  public void setConnectionInfo(WifiInfo wifiInfo) {
    this.wifiInfo = wifiInfo;
  }

  @Implementation
  protected List<ScanResult> getScanResults() {
    return scanResults;
  }

  @Implementation
  protected List<WifiConfiguration> getConfiguredNetworks() {
    final ArrayList<WifiConfiguration> wifiConfigurations = new ArrayList<>();
    for (WifiConfiguration wifiConfiguration : networkIdToConfiguredNetworks.values()) {
      wifiConfigurations.add(wifiConfiguration);
    }
    return wifiConfigurations;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected List<WifiConfiguration> getPrivilegedConfiguredNetworks() {
    return getConfiguredNetworks();
  }

  @Implementation
  protected int addNetwork(WifiConfiguration config) {
    int networkId = networkIdToConfiguredNetworks.size();
    config.networkId = -1;
    networkIdToConfiguredNetworks.put(networkId, makeCopy(config, networkId));
    return networkId;
  }

  @Implementation
  protected boolean removeNetwork(int netId) {
    networkIdToConfiguredNetworks.remove(netId);
    return true;
  }

  @Implementation
  protected int updateNetwork(WifiConfiguration config) {
    if (config == null || config.networkId < 0) {
      return -1;
    }
    networkIdToConfiguredNetworks.put(config.networkId, makeCopy(config, config.networkId));
    return config.networkId;
  }

  @Implementation
  protected boolean saveConfiguration() {
    wasSaved = true;
    return true;
  }

  @Implementation
  protected boolean enableNetwork(int netId, boolean disableOthers) {
    lastEnabledNetwork = new Pair<>(netId, disableOthers);
    return true;
  }

  @Implementation
  protected WifiManager.WifiLock createWifiLock(int lockType, java.lang.String tag) {
    return ReflectionHelpers.callConstructor(WifiManager.WifiLock.class);
  }

  @Implementation
  protected WifiManager.WifiLock createWifiLock(java.lang.String tag) {
    return createWifiLock(WifiManager.WIFI_MODE_FULL, tag);
  }

  @Implementation
  protected static int calculateSignalLevel(int rssi, int numLevels) {
    return (int) (sSignalLevelInPercent * (numLevels - 1));
  }

  @Implementation
  protected boolean startScan() {
    return true;
  }

  @Implementation
  protected DhcpInfo getDhcpInfo() {
    return dhcpInfo;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected boolean isScanAlwaysAvailable() {
    return isScanAlwaysAvailable;
  }

  public static void setSignalLevelInPercent(float level) {
    if (level < 0 || level > 1) {
      throw new IllegalArgumentException("level needs to be between 0 and 1");
    }
    sSignalLevelInPercent = level;
  }

  public void setAccessWifiStatePermission(boolean accessWifiStatePermission) {
    this.accessWifiStatePermission = accessWifiStatePermission;
  }

  public void setScanResults(List<ScanResult> scanResults) {
    this.scanResults = scanResults;
  }

  public void setDhcpInfo(DhcpInfo dhcpInfo) {
    this.dhcpInfo = dhcpInfo;
  }

  public Pair<Integer, Boolean> getLastEnabledNetwork() {
    return lastEnabledNetwork;
  }

  public boolean wasConfigurationSaved() {
    return wasSaved;
  }

  public void setIsScanAlwaysAvailable(boolean isScanAlwaysAvailable) {
    this.isScanAlwaysAvailable = isScanAlwaysAvailable;
  }

  private void checkAccessWifiStatePermission() {
    if (!accessWifiStatePermission) {
      throw new SecurityException();
    }
  }

  private WifiConfiguration makeCopy(WifiConfiguration config, int networkId) {
    WifiConfiguration copy = Shadows.shadowOf(config).copy();
    copy.networkId = networkId;
    return copy;
  }

  @Implements(WifiManager.WifiLock.class)
  public static class ShadowWifiLock {
    private int refCount;
    private boolean refCounted = true;
    private boolean locked;
    public static final int MAX_ACTIVE_LOCKS = 50;

    @Implementation
    protected synchronized void acquire() {
      if (refCounted) {
        if (++refCount >= MAX_ACTIVE_LOCKS) throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
      } else {
        locked = true;
      }
    }

    @Implementation
    protected synchronized void release() {
      if (refCounted) {
        if (--refCount < 0) throw new RuntimeException("WifiLock under-locked");
      } else {
        locked = false;
      }
    }

    @Implementation
    protected synchronized boolean isHeld() {
      return refCounted ? refCount > 0 : locked;
    }

    @Implementation
    protected void setReferenceCounted(boolean refCounted) {
      this.refCounted = refCounted;
    }
  }
}
