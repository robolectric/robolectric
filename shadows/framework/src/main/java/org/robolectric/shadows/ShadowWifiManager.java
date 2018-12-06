package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow for {@link android.net.wifi.WifiManager}.
 */
@Implements(WifiManager.class)
public class ShadowWifiManager {
  private static final int LOCAL_HOST = 2130706433;

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
  private boolean startScanSucceeds = true;
  private boolean is5GHzBandSupported = false;
  private AtomicInteger activeLockCount = new AtomicInteger(0);
  @RealObject WifiManager wifiManager;

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

  @Implementation(minSdk = LOLLIPOP)
  protected boolean is5GHzBandSupported() {
    return is5GHzBandSupported;
  }

  /** Sets whether 5ghz band is supported. */
  public void setIs5GHzBandSupported(boolean is5GHzBandSupported) {
    this.is5GHzBandSupported = is5GHzBandSupported;
  }

  /**
   * Sets the connection info as the provided {@link WifiInfo}.
   */
  public void setConnectionInfo(WifiInfo wifiInfo) {
    this.wifiInfo = wifiInfo;
  }

  /** Sets the return value of {@link #startScan}. */
  public void setStartScanSucceeds(boolean succeeds) {
    this.startScanSucceeds = succeeds;
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
  protected WifiManager.WifiLock createWifiLock(int lockType, String tag) {
    WifiManager.WifiLock wifiLock = ReflectionHelpers.callConstructor(WifiManager.WifiLock.class);
    shadowOf(wifiLock).setWifiManager(wifiManager);
    return wifiLock;
  }

  @Implementation
  protected WifiManager.WifiLock createWifiLock(String tag) {
    return createWifiLock(WifiManager.WIFI_MODE_FULL, tag);
  }

  @Implementation
  protected MulticastLock createMulticastLock(String tag) {
    MulticastLock multicastLock = ReflectionHelpers.callConstructor(MulticastLock.class);
    shadowOf(multicastLock).setWifiManager(wifiManager);
    return multicastLock;
  }

  @Implementation
  protected static int calculateSignalLevel(int rssi, int numLevels) {
    return (int) (sSignalLevelInPercent * (numLevels - 1));
  }

  /**
   * Does nothing and returns the configured success status.
   *
   * <p>That is different from the Android implementation which always returns {@code true} up to
   * and including Android 8, and either {@code true} or {@code false} on Android 9+.
   *
   * @return the value configured by {@link #setStartScanSucceeds}, or {@code true} if that method
   *     was never called.
   */
  @Implementation
  protected boolean startScan() {
    return startScanSucceeds;
  }

  @Implementation
  protected DhcpInfo getDhcpInfo() {
    return dhcpInfo;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected boolean isScanAlwaysAvailable() {
    return isScanAlwaysAvailable;
  }

  @HiddenApi
  @Implementation(minSdk = KITKAT)
  protected void connect(WifiConfiguration wifiConfiguration, WifiManager.ActionListener listener) {
    WifiInfo wifiInfo = getConnectionInfo();

    String ssid = isQuoted(wifiConfiguration.SSID)
        ? stripQuotes(wifiConfiguration.SSID)
        : wifiConfiguration.SSID;

    ShadowWifiInfo shadowWifiInfo = Shadow.extract(wifiInfo);
    shadowWifiInfo.setSSID(ssid);
    shadowWifiInfo.setBSSID(wifiConfiguration.BSSID);
    shadowWifiInfo.setNetworkId(wifiConfiguration.networkId);
    setConnectionInfo(wifiInfo);

    // Now that we're "connected" to wifi, update Dhcp and point it to localhost.
    DhcpInfo dhcpInfo = new DhcpInfo();
    dhcpInfo.gateway = LOCAL_HOST;
    dhcpInfo.ipAddress = LOCAL_HOST;
    setDhcpInfo(dhcpInfo);

    // Now add the network to ConnectivityManager.
    NetworkInfo networkInfo =
        ShadowNetworkInfo.newInstance(
            NetworkInfo.DetailedState.CONNECTED,
            ConnectivityManager.TYPE_WIFI,
            0 /* subType */,
            true /* isAvailable */,
            true /* isConnected */);
    ShadowConnectivityManager connectivityManager =
        Shadow.extract(
                    RuntimeEnvironment.application.getSystemService(Context.CONNECTIVITY_SERVICE));
    connectivityManager.setActiveNetworkInfo(networkInfo);

    if (listener != null) {
      listener.onSuccess();
    }
  }

  @HiddenApi
  @Implementation(minSdk = KITKAT)
  protected void connect(int networkId, WifiManager.ActionListener listener) {
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    wifiConfiguration.networkId = networkId;
    wifiConfiguration.SSID = "";
    wifiConfiguration.BSSID = "";
    connect(wifiConfiguration, listener);
  }

  private static boolean isQuoted(String str) {
    if (str == null || str.length() < 2) {
      return false;
    }

    return str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"';
  }

  private static String stripQuotes(String str) {
    return str.substring(1, str.length() - 1);
  }

  @Implementation
  protected boolean reconnect() {
    WifiConfiguration wifiConfiguration = getMostRecentNetwork();
    if (wifiConfiguration == null) {
      return false;
    }

    connect(wifiConfiguration, null);
    return true;
  }

  private WifiConfiguration getMostRecentNetwork() {
    if (getLastEnabledNetwork() == null) {
      return null;
    }

    return getWifiConfiguration(getLastEnabledNetwork().first);
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

  /** Returns the number of WifiLocks and MulticastLocks that are currently acquired. */
  public int getActiveLockCount() {
    return activeLockCount.get();
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
    ShadowWifiConfiguration shadowWifiConfiguration = Shadow.extract(config);
    WifiConfiguration copy = shadowWifiConfiguration.copy();
    copy.networkId = networkId;
    return copy;
  }

  public WifiConfiguration getWifiConfiguration(int netId) {
    return networkIdToConfiguredNetworks.get(netId);
  }

  @Implements(WifiManager.WifiLock.class)
  public static class ShadowWifiLock {
    private int refCount;
    private boolean refCounted = true;
    private boolean locked;
    private WifiManager wifiManager;
    public static final int MAX_ACTIVE_LOCKS = 50;

    private void setWifiManager(WifiManager wifiManager) {
      this.wifiManager = wifiManager;
    }

    @Implementation
    protected synchronized void acquire() {
      if (wifiManager != null) {
        shadowOf(wifiManager).activeLockCount.getAndIncrement();
      }
      if (refCounted) {
        if (++refCount >= MAX_ACTIVE_LOCKS) throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
      } else {
        locked = true;
      }
    }

    @Implementation
    protected synchronized void release() {
      if (wifiManager != null) {
        shadowOf(wifiManager).activeLockCount.getAndDecrement();
      }
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

  @Implements(MulticastLock.class)
  public static class ShadowMulticastLock {
    private int refCount;
    private boolean refCounted = true;
    private boolean locked;
    static final int MAX_ACTIVE_LOCKS = 50;
    private WifiManager wifiManager;

    private void setWifiManager(WifiManager wifiManager) {
      this.wifiManager = wifiManager;
    }

    @Implementation
    protected void acquire() {
      if (wifiManager != null) {
        shadowOf(wifiManager).activeLockCount.getAndIncrement();
      }
      if (refCounted) {
        if (++refCount >= MAX_ACTIVE_LOCKS) throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
      } else {
        locked = true;
      }
    }

    @Implementation
    protected synchronized void release() {
      if (wifiManager != null) {
        shadowOf(wifiManager).activeLockCount.getAndDecrement();
      }
      if (refCounted) {
        if (--refCount < 0) throw new RuntimeException("WifiLock under-locked");
      } else {
        locked = false;
      }
    }

    @Implementation
    protected void setReferenceCounted(boolean refCounted) {
      this.refCounted = refCounted;
    }

    @Implementation
    protected synchronized boolean isHeld() {
      return refCounted ? refCount > 0 : locked;
    }
  }

  private static ShadowWifiLock shadowOf(WifiManager.WifiLock o) {
    return Shadow.extract(o);
  }

  private static ShadowMulticastLock shadowOf(WifiManager.MulticastLock o) {
    return Shadow.extract(o);
  }

  private static ShadowWifiManager shadowOf(WifiManager o) {
    return Shadow.extract(o);
  }
}
