package com.xtremelabs.robolectric.shadows;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@code NetworkInfo} which is used by ShadowConnectivityManager.
 */

@Implements(NetworkInfo.class)
public class ShadowNetworkInfo {
    private boolean isAvailable = true;
    private boolean isConnected = true;
    private int connectionType = ConnectivityManager.TYPE_MOBILE;

    @Implementation
    public boolean isConnected() {
        return isConnected;
    }

    @Implementation
    public boolean isConnectedOrConnecting() {
        return isConnected;
    }

    @Implementation
    public NetworkInfo.State getState() {
      return isConnected ? NetworkInfo.State.CONNECTED :
          NetworkInfo.State.DISCONNECTED;
    }

    @Implementation
    public int getType(){
    	return connectionType;
    }

    /**
     * Non-Android accessor
     * Sets up the return value of {@link #isAvailable()}.
     *
     * @param isAvailable the value that {@link #isAvailable()} will return.
     */
    public void setAvailableStatus(boolean isAvailable) {
        this.isAvailable = isAvailable;
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
     * Sets up the return value of {@link #getType()}.
     *
     * @param connectionType the value that {@link #getType()} will return.
     */
    public void setConnectionType(int connectionType){
    	this.connectionType = connectionType;
    }
}
