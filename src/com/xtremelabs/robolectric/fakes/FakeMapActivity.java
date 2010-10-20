package com.xtremelabs.robolectric.fakes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.google.android.maps.MapActivity;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapActivity.class)
public class FakeMapActivity extends FakeActivity {
    private ConnectivityBroadcastReceiver connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();

    public FakeMapActivity(MapActivity realActivity) {
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
