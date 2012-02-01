package com.xtremelabs.robolectric.shadows;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

/**
 * Shadow of {@code ConnectivityManager} that provides for the simulation of
 * the active connection status.
 */

@Implements(ConnectivityManager.class)
public class ShadowConnectivityManager {

    private NetworkInfo activeNetwork;
    private NetworkInfo[] networkInfo;
    private boolean backgroundDataSetting;

    private Map<Integer, NetworkInfo> networkTypeToNetworkInfo = new HashMap<Integer, NetworkInfo>();

    @Implementation
    public NetworkInfo getActiveNetworkInfo() {
        return activeNetwork == null ? activeNetwork = newInstanceOf(NetworkInfo.class) : activeNetwork;
    }

    @Implementation
    public NetworkInfo[] getAllNetworkInfo() {
        return networkInfo == null ? networkInfo = new NetworkInfo[]{getActiveNetworkInfo()} : networkInfo;
    }

    @Implementation
    public NetworkInfo getNetworkInfo(int networkType) {
        return networkTypeToNetworkInfo.get(networkType);
    }

    public void setNetworkInfo(int networkType, NetworkInfo networkInfo) {
        networkTypeToNetworkInfo.put(networkType, networkInfo);
    }

    @Implementation
    public boolean getBackgroundDataSetting() {
        return backgroundDataSetting;
    }

    public void setBackgroundDataSetting(boolean b) {
        backgroundDataSetting = b;
    }
}
