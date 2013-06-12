package org.robolectric.shadows;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashMap;
import java.util.Map;

/**
 * Shadow of {@code ConnectivityManager} that provides for the simulation of
 * the active connection status.
 */

@Implements(ConnectivityManager.class)
public class ShadowConnectivityManager {

  private NetworkInfo activeNetwork;
  private boolean backgroundDataSetting;
  private int networkPreference = ConnectivityManager.DEFAULT_NETWORK_PREFERENCE;

  private Map<Integer, NetworkInfo> networkTypeToNetworkInfo = new HashMap<Integer, NetworkInfo>();

  public ShadowConnectivityManager() {
    setActiveNetworkInfo(ShadowNetworkInfo.newInstance());
  }

  @Implementation
  public NetworkInfo getActiveNetworkInfo() {
    return activeNetwork;
  }

  @Implementation
  public NetworkInfo[] getAllNetworkInfo() {
    return networkTypeToNetworkInfo.values().toArray(new NetworkInfo[networkTypeToNetworkInfo.size()]);
  }

  @Implementation
  public NetworkInfo getNetworkInfo(int networkType) {
    return networkTypeToNetworkInfo.get(networkType);
  }

  @Implementation
  public boolean getBackgroundDataSetting() {
    return backgroundDataSetting;
  }

  @Implementation
  public void setNetworkPreference(int preference) {
    networkPreference = preference;
  }

  @Implementation
  public int getNetworkPreference() {
    return networkPreference;
  }

  public void setNetworkInfo(int networkType, NetworkInfo networkInfo) {
    networkTypeToNetworkInfo.put(networkType, networkInfo);
  }

  public void setBackgroundDataSetting(boolean b) {
    backgroundDataSetting = b;
  }

  public void setActiveNetworkInfo(NetworkInfo info) {
    activeNetwork = info;
    if (info != null) {
      networkTypeToNetworkInfo.put(info.getType(), info);
    }  else {
      networkTypeToNetworkInfo.clear();
    }
  }
}
