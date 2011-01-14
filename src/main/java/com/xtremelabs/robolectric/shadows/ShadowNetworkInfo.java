package com.xtremelabs.robolectric.shadows;

import android.net.NetworkInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@code NetworkInfo} which is used by ShadowConnectivityManager.
 */

@Implements(NetworkInfo.class)
public class ShadowNetworkInfo {

    private boolean isConnected = true;

    @Implementation
    public boolean isConnectedOrConnecting() {
        return isConnected;
    }

    /**
     * Non-Android accessor
     * Sets up the return value of {@link #isConnectedOrConnecting()}.
     *
     * @param isConnected the value that {@link #isConnectedOrConnecting()} will return.
     */
    public void setConnectionStatus(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
