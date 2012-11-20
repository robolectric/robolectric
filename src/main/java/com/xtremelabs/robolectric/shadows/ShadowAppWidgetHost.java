package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.content.Context;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(AppWidgetHost.class)
public class ShadowAppWidgetHost {

    private Context context;

    public void __constructor__(Context context, int hostId) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
