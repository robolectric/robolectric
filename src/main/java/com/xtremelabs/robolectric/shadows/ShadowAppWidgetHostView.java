package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(AppWidgetHostView.class)
public class ShadowAppWidgetHostView extends ShadowFrameLayout {

    @RealObject
    private AppWidgetHostView realAppWidgetHostView;
    private int appWidgetId;

    @Implementation
    public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        this.appWidgetId = appWidgetId;
    }

    @Implementation
    public int getAppWidgetId() {
        return appWidgetId;
    }
}
