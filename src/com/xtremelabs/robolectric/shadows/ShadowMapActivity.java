package com.xtremelabs.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.google.android.maps.MapActivity;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapActivity.class)
public class ShadowMapActivity extends ShadowActivity {
    private ConnectivityBroadcastReceiver connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();

    public ShadowMapActivity(MapActivity realActivity) {
        super(realActivity);
    }

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
