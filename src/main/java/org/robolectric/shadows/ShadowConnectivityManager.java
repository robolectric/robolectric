package org.robolectric.shadows;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.HiddenApi;

import java.util.HashMap;
import java.util.Map;

@Implements(ConnectivityManager.class)
public class ShadowConnectivityManager {

  private NetworkInfo activeNetwork;
  private boolean backgroundDataSetting;
  private int networkPreference = ConnectivityManager.DEFAULT_NETWORK_PREFERENCE;
  private final Map<Integer, NetworkInfo> networkTypeToNetworkInfo = new HashMap<Integer, NetworkInfo>();

  public ShadowConnectivityManager() {
    NetworkInfo wifi = ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.DISCONNECTED, ConnectivityManager.TYPE_WIFI, 0, true, false);
    networkTypeToNetworkInfo.put(ConnectivityManager.TYPE_WIFI, wifi);

    NetworkInfo mobile = ShadowNetworkInfo.newInstance(NetworkInfo.DetailedState.CONNECTED, ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_MOBILE_MMS, true, true);
    networkTypeToNetworkInfo.put(ConnectivityManager.TYPE_MOBILE, mobile);

    this.activeNetwork = mobile;
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

  @HiddenApi @Implementation
  public void setBackgroundDataSetting(boolean b) {
    backgroundDataSetting = b;
  }

  public void setActiveNetworkInfo(NetworkInfo info) {
    activeNetwork = info;
    if (info != null) {
      networkTypeToNetworkInfo.put(info.getType(), info);
    } else {
      networkTypeToNetworkInfo.clear();
    }
  }
}
