package com.xtremelabs.robolectric;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RobolectricConfigs {

    public static class ConfigTestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}
