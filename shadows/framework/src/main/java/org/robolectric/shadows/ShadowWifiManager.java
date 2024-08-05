package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static java.util.stream.Collectors.toList;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.AddNetworkResult;
import android.net.wifi.WifiManager.LocalOnlyConnectionFailureListener;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.WifiSsid;
import android.net.wifi.WifiUsabilityStatsEntry;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Pair;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.net.wifi.WifiManager}. */
@Implements(value = WifiManager.class, looseSignatures = true)
@SuppressWarnings("AndroidConcurrentHashMap")
public class ShadowWifiManager {
  private static final int LOCAL_HOST = 2130706433;

  private static float sSignalLevelInPercent = 1f;
  private boolean accessWifiStatePermission = true;
  private boolean changeWifiStatePermission = true;
  private int wifiState = WifiManager.WIFI_STATE_ENABLED;
  private boolean wasSaved = false;
  private WifiInfo wifiInfo;
  private List<ScanResult> scanResults;
  private final Map<Integer, WifiConfiguration> networkIdToConfiguredNetworks =
      new LinkedHashMap<>();
  private Pair<Integer, Boolean> lastEnabledNetwork;
  private final Set<Integer> enabledNetworks = new HashSet<>();
  private DhcpInfo dhcpInfo;
  private boolean startScanSucceeds = true;
  private boolean is5GHzBandSupported = false;
  private boolean isStaApConcurrencySupported = false;
  private boolean isWpa3SaeSupported = false;
  private boolean isWpa3SaeH2eSupported = false;
  private boolean isWpa3SaePublicKeySupported = false;
  private boolean isWpa3SuiteBSupported = false;
  private AtomicInteger activeLockCount = new AtomicInteger(0);
  private final BitSet readOnlyNetworkIds = new BitSet();
  private final ConcurrentHashMap<WifiManager.OnWifiUsabilityStatsListener, Executor>
      wifiUsabilityStatsListeners = new ConcurrentHashMap<>();
  private final List<WifiUsabilityScore> usabilityScores = new ArrayList<>();
  private Object networkScorer;
  @RealObject WifiManager wifiManager;
  private WifiConfiguration apConfig;
  private SoftApConfiguration softApConfig;
  private final Object pnoRequestLock = new Object();
  private PnoScanRequest outstandingPnoScanRequest = null;
  private ImmutableList<WifiNetworkSuggestion> lastAddedSuggestions = ImmutableList.of();
  private int addNetworkSuggestionsResult;

  private final ConcurrentMap<LocalOnlyConnectionFailureListener, Executor>
      localOnlyConnectionFailureListenerExecutorMap = new ConcurrentHashMap<>();

  /**
   * Simulates a connection failure for a specified local network connection.
   *
   * @param specifier the {@link WifiNetworkSpecifier} describing the local network connection
   *     attempt
   * @param failureReason the reason for the network connection failure. This should be one of the
   *     values specified in {@code WifiManager#STATUS_LOCAL_ONLY_CONNECTION_FAILURE_*}
   */
  public void triggerLocalConnectionFailure(WifiNetworkSpecifier specifier, int failureReason) {
    localOnlyConnectionFailureListenerExecutorMap.forEach(
        (failureListener, executor) ->
            executor.execute(() -> failureListener.onConnectionFailed(specifier, failureReason)));
  }

  /** Uses the given result as the return value for {@link WifiManager#addNetworkSuggestions}. */
  public void setAddNetworkSuggestionsResult(int result) {
    addNetworkSuggestionsResult = result;
  }

  @Implementation(minSdk = Q)
  protected int addNetworkSuggestions(List<WifiNetworkSuggestion> networkSuggestions) {
    Preconditions.checkNotNull(networkSuggestions);
    if (addNetworkSuggestionsResult == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
      lastAddedSuggestions = ImmutableList.copyOf(networkSuggestions);
    }
    return addNetworkSuggestionsResult;
  }

  @Implementation(minSdk = R)
  protected List<WifiNetworkSuggestion> getNetworkSuggestions() {
    return lastAddedSuggestions;
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void addLocalOnlyConnectionFailureListener(
      Executor executor, LocalOnlyConnectionFailureListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Listener cannot be null");
    }
    if (executor == null) {
      throw new IllegalArgumentException("Executor cannot be null");
    }
    localOnlyConnectionFailureListenerExecutorMap.putIfAbsent(listener, executor);
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void removeLocalOnlyConnectionFailureListener(
      LocalOnlyConnectionFailureListener listener) {
    if (listener == null) {
      throw new IllegalArgumentException("Listener cannot be null");
    }
    localOnlyConnectionFailureListenerExecutorMap.remove(listener);
  }

  @Implementation
  protected boolean setWifiEnabled(boolean wifiEnabled) {
    checkAccessWifiStatePermission();
    this.wifiState = wifiEnabled ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;
    return true;
  }

  public void setWifiState(int wifiState) {
    checkAccessWifiStatePermission();
    this.wifiState = wifiState;
  }

  @Implementation
  protected boolean isWifiEnabled() {
    checkAccessWifiStatePermission();
    return wifiState == WifiManager.WIFI_STATE_ENABLED;
  }

  @Implementation
  protected int getWifiState() {
    checkAccessWifiStatePermission();
    return wifiState;
  }

  @Implementation
  protected WifiInfo getConnectionInfo() {
    checkAccessWifiStatePermission();
    if (wifiInfo == null) {
      wifiInfo = ReflectionHelpers.callConstructor(WifiInfo.class);
    }
    return wifiInfo;
  }

  @Implementation
  protected boolean is5GHzBandSupported() {
    return is5GHzBandSupported;
  }

  /** Sets whether 5ghz band is supported. */
  public void setIs5GHzBandSupported(boolean is5GHzBandSupported) {
    this.is5GHzBandSupported = is5GHzBandSupported;
  }

  /** Returns last value provided to {@link #setStaApConcurrencySupported}. */
  @Implementation(minSdk = R)
  protected boolean isStaApConcurrencySupported() {
    return isStaApConcurrencySupported;
  }

  /** Sets whether STA/AP concurrency is supported. */
  public void setStaApConcurrencySupported(boolean isStaApConcurrencySupported) {
    this.isStaApConcurrencySupported = isStaApConcurrencySupported;
  }

  /** Returns last value provided to {@link #setWpa3SaeSupported}. */
  @Implementation(minSdk = Q)
  protected boolean isWpa3SaeSupported() {
    return isWpa3SaeSupported;
  }

  /** Sets whether WPA3-Personal SAE is supported. */
  public void setWpa3SaeSupported(boolean isWpa3SaeSupported) {
    this.isWpa3SaeSupported = isWpa3SaeSupported;
  }

  /** Returns last value provided to {@link #setWpa3SaePublicKeySupported}. */
  @Implementation(minSdk = S)
  protected boolean isWpa3SaePublicKeySupported() {
    return isWpa3SaePublicKeySupported;
  }

  /** Sets whether WPA3 SAE Public Key is supported. */
  public void setWpa3SaePublicKeySupported(boolean isWpa3SaePublicKeySupported) {
    this.isWpa3SaePublicKeySupported = isWpa3SaePublicKeySupported;
  }

  /** Returns last value provided to {@link #setWpa3SaeH2eSupported}. */
  @Implementation(minSdk = S)
  protected boolean isWpa3SaeH2eSupported() {
    return isWpa3SaeH2eSupported;
  }

  /** Sets whether WPA3 SAE Hash-to-Element is supported. */
  public void setWpa3SaeH2eSupported(boolean isWpa3SaeH2eSupported) {
    this.isWpa3SaeH2eSupported = isWpa3SaeH2eSupported;
  }

  /** Returns last value provided to {@link #setWpa3SuiteBSupported}. */
  @Implementation(minSdk = Q)
  protected boolean isWpa3SuiteBSupported() {
    return isWpa3SuiteBSupported;
  }

  /** Sets whether WPA3-Enterprise Suite-B-192 is supported. */
  public void setWpa3SuiteBSupported(boolean isWpa3SuiteBSupported) {
    this.isWpa3SuiteBSupported = isWpa3SuiteBSupported;
  }

  /** Sets the connection info as the provided {@link WifiInfo}. */
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

  /**
   * The original implementation allows this to be called by the Device Owner (DO), Profile Owner
   * (PO), callers with carrier privilege and system apps, but this shadow can be called by all apps
   * carrying the ACCESS_WIFI_STATE permission.
   *
   * <p>This shadow is a wrapper for getConfiguredNetworks() and does not actually check the caller.
   */
  @Implementation(minSdk = S)
  protected List<WifiConfiguration> getCallerConfiguredNetworks() {
    checkAccessWifiStatePermission();
    return getConfiguredNetworks();
  }

  @Implementation
  protected List<WifiConfiguration> getConfiguredNetworks() {
    final ArrayList<WifiConfiguration> wifiConfigurations = new ArrayList<>();
    for (WifiConfiguration wifiConfiguration : networkIdToConfiguredNetworks.values()) {
      wifiConfigurations.add(wifiConfiguration);
    }
    return wifiConfigurations;
  }

  @Implementation
  protected List<WifiConfiguration> getPrivilegedConfiguredNetworks() {
    return getConfiguredNetworks();
  }

  @Implementation
  protected int addNetwork(WifiConfiguration config) {
    if (config == null) {
      return -1;
    }
    int networkId = networkIdToConfiguredNetworks.size();
    config.networkId = -1;
    networkIdToConfiguredNetworks.put(networkId, makeCopy(config, networkId));
    return networkId;
  }

  /**
   * The new version of {@link #addNetwork(WifiConfiguration)} which returns a more detailed failure
   * codes. The original implementation of this API is limited to Device Owner (DO), Profile Owner
   * (PO), system app, and privileged apps but this shadow can be called by all apps.
   */
  @Implementation(minSdk = S)
  protected AddNetworkResult addNetworkPrivileged(WifiConfiguration config) {
    if (config == null) {
      throw new IllegalArgumentException("config cannot be null");
    }

    int networkId = addNetwork(config);
    return new AddNetworkResult(AddNetworkResult.STATUS_SUCCESS, networkId);
  }

  @Implementation
  protected boolean removeNetwork(int netId) {
    networkIdToConfiguredNetworks.remove(netId);
    return true;
  }

  /**
   * Removes all configured networks regardless of the app that created the network. Can only be
   * called by a Device Owner (DO) app.
   *
   * @return {@code true} if at least one network is removed, {@code false} otherwise
   */
  @Implementation(minSdk = S)
  protected boolean removeNonCallerConfiguredNetworks() {
    checkChangeWifiStatePermission();
    checkDeviceOwner();
    int previousSize = networkIdToConfiguredNetworks.size();
    networkIdToConfiguredNetworks.clear();
    return networkIdToConfiguredNetworks.size() < previousSize;
  }

  /**
   * Adds or updates a network which can later be retrieved with {@link #getWifiConfiguration(int)}
   * method. A null {@param config}, or one with a networkId less than 0, or a networkId that had
   * its updatePermission removed using the {@link #setUpdateNetworkPermission(int, boolean)} will
   * return -1, which indicates a failure to update.
   */
  @Implementation
  protected int updateNetwork(WifiConfiguration config) {
    if (config == null || config.networkId < 0 || readOnlyNetworkIds.get(config.networkId)) {
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
  protected boolean enableNetwork(int netId, boolean attemptConnect) {
    lastEnabledNetwork = new Pair<>(netId, attemptConnect);
    enabledNetworks.add(netId);
    return true;
  }

  @Implementation
  protected boolean disableNetwork(int netId) {
    return enabledNetworks.remove(netId);
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
    if (getScanResults() != null && !getScanResults().isEmpty()) {
      new Handler(Looper.getMainLooper())
          .post(
              () -> {
                Intent intent = new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                RuntimeEnvironment.getApplication().sendBroadcast(intent);
              });
    }
    return startScanSucceeds;
  }

  @Implementation
  protected DhcpInfo getDhcpInfo() {
    return dhcpInfo;
  }

  @Implementation
  protected boolean isScanAlwaysAvailable() {
    return Settings.Global.getInt(
            getContext().getContentResolver(), Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 1)
        == 1;
  }

  @HiddenApi
  @Implementation
  protected void connect(WifiConfiguration wifiConfiguration, WifiManager.ActionListener listener) {
    WifiInfo wifiInfo = getConnectionInfo();

    String ssid =
        isQuoted(wifiConfiguration.SSID)
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
            RuntimeEnvironment.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE));
    connectivityManager.setActiveNetworkInfo(networkInfo);

    if (listener != null) {
      listener.onSuccess();
    }
  }

  @HiddenApi
  @Implementation
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

  public void setChangeWifiStatePermission(boolean changeWifiStatePermission) {
    this.changeWifiStatePermission = changeWifiStatePermission;
  }

  /**
   * Prevents a networkId from being updated using the {@link updateNetwork(WifiConfiguration)}
   * method. This is to simulate the case where a separate application creates a network, and the
   * Android security model prevents your application from updating it.
   */
  public void setUpdateNetworkPermission(int networkId, boolean hasPermission) {
    readOnlyNetworkIds.set(networkId, !hasPermission);
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

  /** Whether the network is enabled or not. */
  public boolean isNetworkEnabled(int netId) {
    return enabledNetworks.contains(netId);
  }

  /** Returns the number of WifiLocks and MulticastLocks that are currently acquired. */
  public int getActiveLockCount() {
    return activeLockCount.get();
  }

  public boolean wasConfigurationSaved() {
    return wasSaved;
  }

  public void setIsScanAlwaysAvailable(boolean isScanAlwaysAvailable) {
    Settings.Global.putInt(
        getContext().getContentResolver(),
        Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE,
        isScanAlwaysAvailable ? 1 : 0);
  }

  private void checkAccessWifiStatePermission() {
    if (!accessWifiStatePermission) {
      throw new SecurityException("Caller does not hold ACCESS_WIFI_STATE permission");
    }
  }

  private void checkChangeWifiStatePermission() {
    if (!changeWifiStatePermission) {
      throw new SecurityException("Caller does not hold CHANGE_WIFI_STATE permission");
    }
  }

  private void checkDeviceOwner() {
    if (!getContext()
        .getSystemService(DevicePolicyManager.class)
        .isDeviceOwnerApp(getContext().getPackageName())) {
      throw new SecurityException("Caller is not device owner");
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

  @Implementation(minSdk = Q)
  @HiddenApi
  protected void addOnWifiUsabilityStatsListener(Object executorObject, Object listenerObject) {
    Executor executor = (Executor) executorObject;
    WifiManager.OnWifiUsabilityStatsListener listener =
        (WifiManager.OnWifiUsabilityStatsListener) listenerObject;
    wifiUsabilityStatsListeners.put(listener, executor);
  }

  @Implementation(minSdk = Q)
  @HiddenApi
  protected void removeOnWifiUsabilityStatsListener(Object listenerObject) {
    WifiManager.OnWifiUsabilityStatsListener listener =
        (WifiManager.OnWifiUsabilityStatsListener) listenerObject;
    wifiUsabilityStatsListeners.remove(listener);
  }

  @Implementation(minSdk = Q)
  @HiddenApi
  protected void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
    synchronized (usabilityScores) {
      usabilityScores.add(new WifiUsabilityScore(seqNum, score, predictionHorizonSec));
    }
  }

  /**
   * Implements setWifiConnectedNetworkScorer() with the generic Object input as
   * WifiConnectedNetworkScorer is a hidden/System API.
   */
  @Implementation(minSdk = R)
  @HiddenApi
  protected boolean setWifiConnectedNetworkScorer(Object executorObject, Object scorerObject) {
    if (networkScorer == null) {
      networkScorer = scorerObject;
      return true;
    } else {
      return false;
    }
  }

  @Implementation(minSdk = R)
  @HiddenApi
  protected void clearWifiConnectedNetworkScorer() {
    networkScorer = null;
  }

  /** Returns if wifi connected betwork scorer enabled */
  public boolean isWifiConnectedNetworkScorerEnabled() {
    return networkScorer != null;
  }

  @Implementation
  protected boolean setWifiApConfiguration(WifiConfiguration apConfig) {
    this.apConfig = apConfig;
    return true;
  }

  @Implementation
  protected WifiConfiguration getWifiApConfiguration() {
    return apConfig;
  }

  @Implementation(minSdk = R)
  protected boolean setSoftApConfiguration(SoftApConfiguration softApConfig) {
    this.softApConfig = softApConfig;
    return true;
  }

  @Implementation(minSdk = R)
  protected SoftApConfiguration getSoftApConfiguration() {
    return softApConfig;
  }

  /**
   * Returns wifi usability scores previous passed to {@link WifiManager#updateWifiUsabilityScore}
   */
  public List<WifiUsabilityScore> getUsabilityScores() {
    synchronized (usabilityScores) {
      return ImmutableList.copyOf(usabilityScores);
    }
  }

  /**
   * Clears wifi usability scores previous passed to {@link WifiManager#updateWifiUsabilityScore}
   */
  public void clearUsabilityScores() {
    synchronized (usabilityScores) {
      usabilityScores.clear();
    }
  }

  /**
   * Post Wifi stats to any listeners registered with {@link
   * WifiManager#addOnWifiUsabilityStatsListener}
   */
  public void postUsabilityStats(
      int seqNum, boolean isSameBssidAndFreq, WifiUsabilityStatsEntryBuilder statsBuilder) {
    WifiUsabilityStatsEntry stats = statsBuilder.build();

    Set<Map.Entry<WifiManager.OnWifiUsabilityStatsListener, Executor>> toNotify = new ArraySet<>();
    toNotify.addAll(wifiUsabilityStatsListeners.entrySet());
    for (Map.Entry<WifiManager.OnWifiUsabilityStatsListener, Executor> entry : toNotify) {
      entry
          .getValue()
          .execute(
              new Runnable() {
                // Using a lambda here means loading the ShadowWifiManager class tries
                // to load the WifiManager.OnWifiUsabilityStatsListener which fails if
                // not building against a system API.
                @Override
                public void run() {
                  entry.getKey().onWifiUsabilityStats(seqNum, isSameBssidAndFreq, stats);
                }
              });
    }
  }

  private Context getContext() {
    return ReflectionHelpers.getField(wifiManager, "mContext");
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
        if (++refCount >= MAX_ACTIVE_LOCKS) {
          throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
        }
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
        if (++refCount >= MAX_ACTIVE_LOCKS) {
          throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
        }
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

  /** Class to record scores passed to WifiManager#updateWifiUsabilityScore */
  public static class WifiUsabilityScore {
    public final int seqNum;
    public final int score;
    public final int predictionHorizonSec;

    private WifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
      this.seqNum = seqNum;
      this.score = score;
      this.predictionHorizonSec = predictionHorizonSec;
    }
  }

  /** Informs the {@link WifiManager} of a list of PNO {@link ScanResult}. */
  public void networksFoundFromPnoScan(List<ScanResult> scanResults) {
    synchronized (pnoRequestLock) {
      List<ScanResult> scanResultsCopy = List.copyOf(scanResults);
      if (outstandingPnoScanRequest == null
          || outstandingPnoScanRequest.ssids.stream()
              .noneMatch(
                  ssid ->
                      scanResultsCopy.stream()
                          .anyMatch(scanResult -> scanResult.getWifiSsid().equals(ssid)))) {
        return;
      }
      Executor executor = outstandingPnoScanRequest.executor;
      InternalPnoScanResultsCallback callback = outstandingPnoScanRequest.callback;
      executor.execute(() -> callback.onScanResultsAvailable(scanResultsCopy));
      Intent intent = createPnoScanResultsBroadcastIntent();
      getContext().sendBroadcast(intent);
      executor.execute(
          () ->
              callback.onRemoved(
                  InternalPnoScanResultsCallback.REMOVE_PNO_CALLBACK_RESULTS_DELIVERED));
      outstandingPnoScanRequest = null;
    }
  }

  // Object needs to be used here since PnoScanResultsCallback is hidden. The looseSignatures spec
  // requires that all args are of type Object.
  @Implementation(minSdk = TIRAMISU)
  @HiddenApi
  protected void setExternalPnoScanRequest(
      Object ssids, Object frequencies, Object executor, Object callback) {
    synchronized (pnoRequestLock) {
      if (callback == null) {
        throw new IllegalArgumentException("callback cannot be null");
      }

      List<WifiSsid> pnoSsids = (List<WifiSsid>) ssids;
      int[] pnoFrequencies = (int[]) frequencies;
      Executor pnoExecutor = (Executor) executor;
      InternalPnoScanResultsCallback pnoCallback = new InternalPnoScanResultsCallback(callback);

      if (pnoExecutor == null) {
        throw new IllegalArgumentException("executor cannot be null");
      }
      if (pnoSsids == null || pnoSsids.isEmpty()) {
        // The real WifiServiceImpl throws an IllegalStateException in this case, so keeping it the
        // same for consistency.
        throw new IllegalStateException("Ssids can't be null or empty");
      }
      if (pnoSsids.size() > 2) {
        throw new IllegalArgumentException("Ssid list can't be greater than 2");
      }
      if (pnoFrequencies != null && pnoFrequencies.length > 10) {
        throw new IllegalArgumentException("Length of frequencies must be smaller than 10");
      }
      int uid = Binder.getCallingUid();
      String packageName = getContext().getPackageName();

      if (outstandingPnoScanRequest != null) {
        pnoExecutor.execute(
            () ->
                pnoCallback.onRegisterFailed(
                    uid == outstandingPnoScanRequest.uid
                        ? InternalPnoScanResultsCallback.REGISTER_PNO_CALLBACK_ALREADY_REGISTERED
                        : InternalPnoScanResultsCallback.REGISTER_PNO_CALLBACK_RESOURCE_BUSY));
        return;
      }

      outstandingPnoScanRequest =
          new PnoScanRequest(pnoSsids, pnoFrequencies, pnoExecutor, pnoCallback, packageName, uid);
      pnoExecutor.execute(pnoCallback::onRegisterSuccess);
    }
  }

  @Implementation(minSdk = TIRAMISU)
  @HiddenApi
  protected void clearExternalPnoScanRequest() {
    synchronized (pnoRequestLock) {
      if (outstandingPnoScanRequest != null
          && outstandingPnoScanRequest.uid == Binder.getCallingUid()) {
        InternalPnoScanResultsCallback callback = outstandingPnoScanRequest.callback;
        outstandingPnoScanRequest.executor.execute(
            () ->
                callback.onRemoved(
                    InternalPnoScanResultsCallback.REMOVE_PNO_CALLBACK_UNREGISTERED));
        outstandingPnoScanRequest = null;
      }
    }
  }

  private static class PnoScanRequest {
    private final List<WifiSsid> ssids;
    private final List<Integer> frequencies;
    private final Executor executor;
    private final InternalPnoScanResultsCallback callback;
    private final String packageName;
    private final int uid;

    private PnoScanRequest(
        List<WifiSsid> ssids,
        int[] frequencies,
        Executor executor,
        InternalPnoScanResultsCallback callback,
        String packageName,
        int uid) {
      this.ssids = List.copyOf(ssids);
      this.frequencies =
          frequencies == null ? List.of() : Arrays.stream(frequencies).boxed().collect(toList());
      this.executor = executor;
      this.callback = callback;
      this.packageName = packageName;
      this.uid = uid;
    }
  }

  private Intent createPnoScanResultsBroadcastIntent() {
    Intent intent = new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    intent.putExtra(WifiManager.EXTRA_RESULTS_UPDATED, true);
    intent.setPackage(outstandingPnoScanRequest.packageName);
    return intent;
  }

  private static class InternalPnoScanResultsCallback {
    static final int REGISTER_PNO_CALLBACK_ALREADY_REGISTERED = 1;
    static final int REGISTER_PNO_CALLBACK_RESOURCE_BUSY = 2;
    static final int REMOVE_PNO_CALLBACK_RESULTS_DELIVERED = 1;
    static final int REMOVE_PNO_CALLBACK_UNREGISTERED = 2;

    final Object callback;
    final Method availableCallback;
    final Method successCallback;
    final Method failedCallback;
    final Method removedCallback;

    InternalPnoScanResultsCallback(Object callback) {
      this.callback = callback;
      try {
        Class<?> pnoCallbackClass = callback.getClass();
        availableCallback = pnoCallbackClass.getMethod("onScanResultsAvailable", List.class);
        successCallback = pnoCallbackClass.getMethod("onRegisterSuccess");
        failedCallback = pnoCallbackClass.getMethod("onRegisterFailed", int.class);
        removedCallback = pnoCallbackClass.getMethod("onRemoved", int.class);
      } catch (NoSuchMethodException e) {
        throw new IllegalArgumentException("callback is not of type PnoScanResultsCallback", e);
      }
    }

    void onScanResultsAvailable(List<ScanResult> scanResults) {
      invokeCallback(availableCallback, scanResults);
    }

    void onRegisterSuccess() {
      invokeCallback(successCallback);
    }

    void onRegisterFailed(int reason) {
      invokeCallback(failedCallback, reason);
    }

    void onRemoved(int reason) {
      invokeCallback(removedCallback, reason);
    }

    void invokeCallback(Method method, Object... args) {
      try {
        method.invoke(callback, args);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalStateException("Failed to invoke " + method.getName(), e);
      }
    }
  }
}
