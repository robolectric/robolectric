package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.net.ConnectivityManager;
import android.net.ConnectivityManager.OnNetworkActiveListener;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(ConnectivityManager.class)
public class ShadowConnectivityManager {

  // Package-private for tests.
  static final int NET_ID_WIFI = ConnectivityManager.TYPE_WIFI;
  static final int NET_ID_MOBILE = ConnectivityManager.TYPE_MOBILE;

  private NetworkInfo activeNetworkInfo;
  private boolean backgroundDataSetting;
  private int networkPreference = ConnectivityManager.DEFAULT_NETWORK_PREFERENCE;
  private final Map<Integer, NetworkInfo> networkTypeToNetworkInfo = new HashMap<>();

  private HashSet<ConnectivityManager.NetworkCallback> networkCallbacks = new HashSet<>();
  private final Map<Integer, Network> netIdToNetwork = new HashMap<>();
  private final Map<Integer, NetworkInfo> netIdToNetworkInfo = new HashMap<>();
  private Network processBoundNetwork;
  private boolean defaultNetworkActive;
  private HashSet<ConnectivityManager.OnNetworkActiveListener> onNetworkActiveListeners =
      new HashSet<>();
  private Map<Network, Boolean> reportedNetworkConnectivity = new HashMap<>();
  private Map<Network, NetworkCapabilities> networkCapabilitiesMap = new HashMap<>();
  private String captivePortalServerUrl = "http://10.0.0.2";
  private final Map<Network, LinkProperties> linkPropertiesMap = new HashMap<>();

  public ShadowConnectivityManager() {
    NetworkInfo wifi = ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.DISCONNECTED,
        ConnectivityManager.TYPE_WIFI, 0, true, false);
    networkTypeToNetworkInfo.put(ConnectivityManager.TYPE_WIFI, wifi);

    NetworkInfo mobile = ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.CONNECTED,
        ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_MOBILE_MMS, true, true);
    networkTypeToNetworkInfo.put(ConnectivityManager.TYPE_MOBILE, mobile);

    this.activeNetworkInfo = mobile;

    if (getApiLevel() >= LOLLIPOP) {
      netIdToNetwork.put(NET_ID_WIFI, ShadowNetwork.newInstance(NET_ID_WIFI));
      netIdToNetwork.put(NET_ID_MOBILE, ShadowNetwork.newInstance(NET_ID_MOBILE));
      netIdToNetworkInfo.put(NET_ID_WIFI, wifi);
      netIdToNetworkInfo.put(NET_ID_MOBILE, mobile);
    }
    defaultNetworkActive = true;
  }

  public Set<ConnectivityManager.NetworkCallback> getNetworkCallbacks() {
    return networkCallbacks;
  }

  /**
   * @return networks and their connectivity status which was reported with {@link
   *     #reportNetworkConnectivity}.
   */
  public Map<Network, Boolean> getReportedNetworkConnectivity() {
    return new HashMap<>(reportedNetworkConnectivity);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void registerNetworkCallback(
      NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback) {
    registerNetworkCallback(request, networkCallback, null);
  }

  @Implementation(minSdk = O)
  protected void registerNetworkCallback(
      NetworkRequest request,
      ConnectivityManager.NetworkCallback networkCallback,
      Handler handler) {
    networkCallbacks.add(networkCallback);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void requestNetwork(
      NetworkRequest request, ConnectivityManager.NetworkCallback networkCallback) {
    registerNetworkCallback(request, networkCallback);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void unregisterNetworkCallback(ConnectivityManager.NetworkCallback networkCallback) {
    if (networkCallback == null) {
      throw new IllegalArgumentException("Invalid NetworkCallback");
    }
    if (networkCallbacks.contains(networkCallback)) {
      networkCallbacks.remove(networkCallback);
    }
  }

  @Implementation
  protected NetworkInfo getActiveNetworkInfo() {
    return activeNetworkInfo;
  }

  /**
   * @see #setActiveNetworkInfo(NetworkInfo)
   * @see #setNetworkInfo(int, NetworkInfo)
   */
  @Implementation(minSdk = M)
  protected Network getActiveNetwork() {
    if (defaultNetworkActive) {
      return netIdToNetwork.get(getActiveNetworkInfo().getType());
    }
    return null;
  }

  /**
   * @see #setActiveNetworkInfo(NetworkInfo)
   * @see #setNetworkInfo(int, NetworkInfo)
   */
  @Implementation
  protected NetworkInfo[] getAllNetworkInfo() {
    // todo(xian): is `defaultNetworkActive` really relevant here?
    if (defaultNetworkActive) {
      return networkTypeToNetworkInfo
          .values()
          .toArray(new NetworkInfo[networkTypeToNetworkInfo.size()]);
    }
    return null;
  }

  @Implementation
  protected NetworkInfo getNetworkInfo(int networkType) {
    return networkTypeToNetworkInfo.get(networkType);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected NetworkInfo getNetworkInfo(Network network) {
    if (network == null) {
      return null;
    }
    ShadowNetwork shadowNetwork = Shadow.extract(network);
    return netIdToNetworkInfo.get(shadowNetwork.getNetId());
  }

  @Implementation(minSdk = LOLLIPOP)
  protected Network[] getAllNetworks() {
    return netIdToNetwork.values().toArray(new Network[netIdToNetwork.size()]);
  }

  @Implementation
  protected boolean getBackgroundDataSetting() {
    return backgroundDataSetting;
  }

  @Implementation
  protected void setNetworkPreference(int preference) {
    networkPreference = preference;
  }

  @Implementation
  protected int getNetworkPreference() {
    return networkPreference;
  }

  /**
   * Counts {@link ConnectivityManager#TYPE_MOBILE} networks as metered. Other types will be
   * considered unmetered.
   *
   * @return `true` if the active network is metered, otherwise `false`.
   * @see #setActiveNetworkInfo(NetworkInfo)
   * @see #setDefaultNetworkActive(boolean)
   */
  @Implementation
  protected boolean isActiveNetworkMetered() {
    if (defaultNetworkActive && activeNetworkInfo != null) {
      return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    } else {
      return false;
    }
  }

  @Implementation(minSdk = M)
  protected boolean bindProcessToNetwork(Network network) {
    processBoundNetwork = network;
    return true;
  }

  @Implementation(minSdk = M)
  protected Network getBoundNetworkForProcess() {
    return processBoundNetwork;
  }

  public void setNetworkInfo(int networkType, NetworkInfo networkInfo) {
    networkTypeToNetworkInfo.put(networkType, networkInfo);
  }

  /**
   * Returns the captive portal URL previously set with {@link #setCaptivePortalServerUrl}.
   */
  @Implementation(minSdk = N)
  protected String getCaptivePortalServerUrl() {
    return captivePortalServerUrl;
  }

  /**
   * Sets the captive portal URL, which will be returned in {@link #getCaptivePortalServerUrl}.
   *
   * @param captivePortalServerUrl the url of captive portal.
   */
  public void setCaptivePortalServerUrl(String captivePortalServerUrl) {
    this.captivePortalServerUrl = captivePortalServerUrl;
  }

  @HiddenApi @Implementation
  public void setBackgroundDataSetting(boolean b) {
    backgroundDataSetting = b;
  }

  public void setActiveNetworkInfo(NetworkInfo info) {
    if (getApiLevel() >= LOLLIPOP) {
      activeNetworkInfo = info;
      if (info != null) {
        networkTypeToNetworkInfo.put(info.getType(), info);
        netIdToNetwork.put(info.getType(), ShadowNetwork.newInstance(info.getType()));
        netIdToNetworkInfo.put(info.getType(), info);
      } else {
        networkTypeToNetworkInfo.clear();
        netIdToNetwork.clear();
      }
    } else {
      activeNetworkInfo = info;
      if (info != null) {
        networkTypeToNetworkInfo.put(info.getType(), info);
      } else {
        networkTypeToNetworkInfo.clear();
      }
    }
  }

  /**
   * Adds new {@code network} to the list of all {@link android.net.Network}s.
   *
   * @param network The network.
   * @param networkInfo The network info paired with the {@link android.net.Network}.
   */
  public void addNetwork(Network network, NetworkInfo networkInfo) {
    ShadowNetwork shadowNetwork = Shadow.extract(network);
    int netId = shadowNetwork.getNetId();
    netIdToNetwork.put(netId, network);
    netIdToNetworkInfo.put(netId, networkInfo);
  }

  /**
   * Removes the {@code network} from the list of all {@link android.net.Network}s.
   * @param network The network.
   */
  public void removeNetwork(Network network) {
    ShadowNetwork shadowNetwork = Shadow.extract(network);
    int netId = shadowNetwork.getNetId();
    netIdToNetwork.remove(netId);
    netIdToNetworkInfo.remove(netId);
  }

  /**
   * Clears the list of all {@link android.net.Network}s.
   */
  public void clearAllNetworks() {
    netIdToNetwork.clear();
    netIdToNetworkInfo.clear();
  }

  /**
   * Sets the active state of the default network.
   *
   * By default this is true and affects the result of {@link
   * ConnectivityManager#isActiveNetworkMetered()}, {@link
   * ConnectivityManager#isDefaultNetworkActive()}, {@link ConnectivityManager#getActiveNetwork()}
   * and {@link ConnectivityManager#getAllNetworkInfo()}.
   *
   * Calling this method with {@code true} after any listeners have been registered with {@link
   * ConnectivityManager#addDefaultNetworkActiveListener(OnNetworkActiveListener)} will result in
   * those listeners being fired.
   *
   * @param isActive The active state of the default network.
   */
  public void setDefaultNetworkActive(boolean isActive) {
    defaultNetworkActive = isActive;
    if (defaultNetworkActive) {
      for (ConnectivityManager.OnNetworkActiveListener l : onNetworkActiveListeners) {
        if (l != null) {
          l.onNetworkActive();
        }
      }
    }
  }

  /**
   * @return `true` by default, or the value specifed via {@link #setDefaultNetworkActive(boolean)}
   * @see #setDefaultNetworkActive(boolean)
   */
  @Implementation(minSdk = LOLLIPOP)
  protected boolean isDefaultNetworkActive() {
    return defaultNetworkActive;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void addDefaultNetworkActiveListener(final ConnectivityManager.OnNetworkActiveListener l) {
    onNetworkActiveListeners.add(l);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void removeDefaultNetworkActiveListener(ConnectivityManager.OnNetworkActiveListener l) {
    if (l == null) {
      throw new IllegalArgumentException("Invalid OnNetworkActiveListener");
    }
    if (onNetworkActiveListeners.contains(l)) {
      onNetworkActiveListeners.remove(l);
    }
  }

  @Implementation(minSdk = M)
  protected void reportNetworkConnectivity(Network network, boolean hasConnectivity) {
    reportedNetworkConnectivity.put(network, hasConnectivity);
  }

  /**
   * Gets the network capabilities of a given {@link Network}.
   *
   * @param network The {@link Network} object identifying the network in question.
   * @return The {@link android.net.NetworkCapabilities} for the network.
   * @see #setNetworkCapabilities(Network, NetworkCapabilities)
   */
  @Implementation(minSdk = LOLLIPOP)
  protected NetworkCapabilities getNetworkCapabilities(Network network) {
    return networkCapabilitiesMap.get(network);
  }

  /**
   * Sets network capability and affects the result of {@link
   * ConnectivityManager#getNetworkCapabilities(Network)}
   *
   * @param network The {@link Network} object identifying the network in question.
   * @param networkCapabilities The {@link android.net.NetworkCapabilities} for the network.
   */
  public void setNetworkCapabilities(Network network, NetworkCapabilities networkCapabilities) {
    networkCapabilitiesMap.put(network, networkCapabilities);
  }

  /**
   * Sets the value for enabling/disabling airplane mode
   *
   * @param enable new status for airplane mode
   */
  @Implementation(minSdk = KITKAT)
  protected void setAirplaneMode(boolean enable) {
    ShadowSettings.setAirplaneMode(enable);
  }

  /** @see #setLinkProperties(Network, LinkProperties) */
  @Implementation(minSdk = LOLLIPOP)
  protected LinkProperties getLinkProperties(Network network) {
    return linkPropertiesMap.get(network);
  }

  /**
   * Sets the LinkProperties for the given Network.
   *
   * <p>A LinkProperties can be constructed by
   * `org.robolectric.util.ReflectionHelpers.callConstructor` in tests.
   */
  public void setLinkProperties(Network network, LinkProperties linkProperties) {
    linkPropertiesMap.put(network, linkProperties);
  }
}
