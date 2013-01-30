package com.xtremelabs.robolectric.shadows;

import android.content.IntentFilter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(IntentFilter.AuthorityEntry.class)
public class ShadowIntentFilterAuthorityEntry {
    private String host;
    private int port;

    public void __constructor__(String host, String port) {
        this.host = host;
        if (port == null) {
            this.port = -1;
        } else {
            this.port = Integer.parseInt(port);
        }
    }

    @Implementation
    public String getHost() {
        return host;
    }

    @Implementation
    public int getPort() {
        return port;
    }
}
