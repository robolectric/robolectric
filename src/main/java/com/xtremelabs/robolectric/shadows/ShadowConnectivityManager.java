package com.xtremelabs.robolectric.shadows;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

/**
 * Shadow of {@code ConnectivityManager} that provides for the simulation of
 * the active connection status.
 */

@Implements(ConnectivityManager.class)
public class ShadowConnectivityManager {

    private NetworkInfo activeNetwork;
    private NetworkInfo[] networkInfo;

    @Implementation
    public NetworkInfo getActiveNetworkInfo() {
        return activeNetwork == null ? activeNetwork = newInstanceOf(NetworkInfo.class) : activeNetwork;
    }

    @Implementation
    public NetworkInfo[] getAllNetworkInfo() {
        return networkInfo == null ? networkInfo = new NetworkInfo[]{getActiveNetworkInfo()} : networkInfo;
    }
}
