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
    private boolean isAvailable = true;

    @Implementation
    public boolean isConnected() {
        return isConnected;
    }

    @Implementation
    public boolean isConnectedOrConnecting() {
        return isConnected;
    }

    @Implementation
    public boolean isAvailable() {
        return isAvailable;
    }

    /**
     * Non-Android accessor
     * Sets up the return value of {@link #isConnectedOrConnecting()} and {@link @isConnected()}.
     *
     * @param isConnected the value that {@link #isConnectedOrConnecting()} and {@link #isConnected()} will return.
     */
    public void setConnectionStatus(boolean isConnected) {
        this.isConnected = isConnected;
    }

    /**
     * Non-Android accessor
     * Sets up the return value of {@link #isAvailable()}.
     *
     * @param isConnected the value that {@link #isAvailable()} will return.
     */
    public void setAvailableStatus(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

}
