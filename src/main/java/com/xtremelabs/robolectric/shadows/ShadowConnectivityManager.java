package com.xtremelabs.robolectric.shadows;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

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

    private Map<Integer, NetworkInfo> networkTypeToNetworkInfo = new HashMap<Integer, NetworkInfo>();

    public void __constructor__() {
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
