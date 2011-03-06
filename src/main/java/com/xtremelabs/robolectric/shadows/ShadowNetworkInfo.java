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

    private boolean isConnected = true;
    private int connectionType = ConnectivityManager.TYPE_MOBILE;

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
    
    @Implementation
    public boolean isConnected() {
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

    /**
     * Non-Android accessor
     * Sets up the return value of {@link #getType()}.
     *
     * @param connectionType the value that {@link #getType()} will return.
     */
    public void setConnectionType(int type){
    	this.connectionType = type;
    }
}
