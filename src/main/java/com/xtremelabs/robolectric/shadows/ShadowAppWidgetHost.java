package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(AppWidgetHost.class)
public class ShadowAppWidgetHost {
    @RealObject
    private AppWidgetHost realAppWidgetHost;

    private Context context;
    private int hostId;
    private int appWidgetIdToAllocate;

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

    public void setAppWidgetIdToAllocate(int idToAllocate) {
        appWidgetIdToAllocate = idToAllocate;
    }

    @Implementation
    public int allocateAppWidgetId() {
        return appWidgetIdToAllocate;
    }

    @Implementation
    public AppWidgetHostView createView(Context context, int appWidgetId,
                                        AppWidgetProviderInfo appWidget) {
        AppWidgetHostView hostView = new AppWidgetHostView(context);
        hostView.setAppWidget(appWidgetId, appWidget);
        shadowOf(hostView).setHost(realAppWidgetHost);
        return hostView;
    }
}
