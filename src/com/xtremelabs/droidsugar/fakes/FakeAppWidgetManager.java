package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.AppSingletonizer;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AppWidgetManager.class)
public class FakeAppWidgetManager {
    private static AppSingletonizer<AppWidgetManager> instances = new AppSingletonizer<AppWidgetManager>(AppWidgetManager.class);

    private AppWidgetManager realAppWidgetManager;
    private Context context;
    private Map<Integer, View> widgetViews = new HashMap<Integer, View>();
    private int nextWidgetId = 1;

    public FakeAppWidgetManager(AppWidgetManager realAppWidgetManager) {
        this.realAppWidgetManager = realAppWidgetManager;
    }

    @Implementation
    public static AppWidgetManager getInstance(Context context) {
        AppWidgetManager appWidgetManager = instances.getInstance(context);
        proxyFor(appWidgetManager).context = context;
        return appWidgetManager;
    }

    @Implementation
    public void updateAppWidget(int[] appWidgetIds, RemoteViews views) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(appWidgetId, views);
        }
    }

    @Implementation
    public void updateAppWidget(int appWidgetId, RemoteViews views) {
        views.reapply(context, getViewFor(appWidgetId));
    }

    public int createWidget(Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId) {
        View widgetView = new Activity().getLayoutInflater().inflate(widgetLayoutId, null);
        widgetViews.put(nextWidgetId, widgetView);

        AppWidgetProvider appWidgetProvider = FakeHelper.newInstanceOf(appWidgetProviderClass);
        appWidgetProvider.onUpdate(context, realAppWidgetManager, new int[] { nextWidgetId });
        return nextWidgetId++;
    }

    public View getViewFor(int widgetId) {
        return widgetViews.get(widgetId);
    }

    private static FakeAppWidgetManager proxyFor(AppWidgetManager appWidgetManager) {
        return ((FakeAppWidgetManager) ProxyDelegatingHandler.getInstance().proxyFor(appWidgetManager));
    }

}
