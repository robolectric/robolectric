package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(AppWidgetHost.class)
public class ShadowAppWidgetHost {

    private Context context;
    private int hostId;

    public void __constructor__(Context context, int hostId) {
        this.context = context;
        this.hostId = hostId;
    }

    public Context getContext() {
        return context;
    }

    public int getHostId() {
        return hostId;
    }
}
