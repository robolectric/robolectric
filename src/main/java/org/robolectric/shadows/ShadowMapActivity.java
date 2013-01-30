package com.xtremelabs.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.google.android.maps.MapActivity;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow {@code MapActivity} that registers and unregisters a {@code BroadcastReciever} when {@link #onResume()} and
 * {@link #onPause()} are called respectively.
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapActivity.class)
public class ShadowMapActivity extends ShadowActivity {
    private ConnectivityBroadcastReceiver connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();

    @Implementation
    public void onResume() {
        registerReceiver(connectivityBroadcastReceiver, new IntentFilter());
    }

    @Implementation
    public void onPause() {
        unregisterReceiver(connectivityBroadcastReceiver);
    }

    @Implementation
    public boolean isRouteDisplayed() {
        return false;
    }

    private static class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
        }
    }
}
