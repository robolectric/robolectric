package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

/**
 * Shadow of {@code ConnectivityManager} that provides for the simulation of
 * the active connection status.
 */

@Implements(ConnectivityManager.class)
public class ShadowConnectivityManager {

	private NetworkInfo ni;

	@Implementation
    public NetworkInfo getActiveNetworkInfo() {
		return ni == null ? ni = newInstanceOf(NetworkInfo.class) : ni;
    }
}
