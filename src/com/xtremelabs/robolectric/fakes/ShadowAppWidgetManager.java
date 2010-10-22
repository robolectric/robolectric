package com.xtremelabs.robolectric.fakes;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.AppSingletonizer;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AppWidgetManager.class)
public class ShadowAppWidgetManager {
    private static AppSingletonizer<AppWidgetManager> instances = new AppSingletonizer<AppWidgetManager>(AppWidgetManager.class) {
        @Override protected AppWidgetManager get(ShadowApplication fakeApplication) {
            return fakeApplication.appWidgetManager;
        }

        @Override protected void set(ShadowApplication fakeApplication, AppWidgetManager instance) {
            fakeApplication.appWidgetManager = instance;
        }

        @Override
        protected AppWidgetManager createInstance(Application applicationContext) {
            AppWidgetManager appWidgetManager = super.createInstance(applicationContext);
            proxyFor(appWidgetManager).context = applicationContext;
            return appWidgetManager;
        }
    };

    private static void bind(AppWidgetManager appWidgetManager, Context context) {
    }

    private AppWidgetManager realAppWidgetManager;
    private Context context;
    private Map<Integer, View> widgetViews = new HashMap<Integer, View>();
    private int nextWidgetId = 1;

    public ShadowAppWidgetManager(AppWidgetManager realAppWidgetManager) {
        this.realAppWidgetManager = realAppWidgetManager;
    }

    @Implementation
    public static AppWidgetManager getInstance(Context context) {
        return instances.getInstance(context);
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

        AppWidgetProvider appWidgetProvider = Robolectric.newInstanceOf(appWidgetProviderClass);
        appWidgetProvider.onUpdate(context, realAppWidgetManager, new int[] { nextWidgetId });
        return nextWidgetId++;
    }

    public List<Integer> createWidgets(int howManyToCreate, Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId) {
        int[] newWidgetIds = new int[howManyToCreate];
        AppWidgetProvider appWidgetProvider = Robolectric.newInstanceOf(appWidgetProviderClass);
        for (int i = 0; i < howManyToCreate; i++) {
            View widgetView = new Activity().getLayoutInflater().inflate(widgetLayoutId, null);
            widgetViews.put(nextWidgetId, widgetView);
            newWidgetIds[i] = nextWidgetId++;
        }
        appWidgetProvider.onUpdate(context, realAppWidgetManager, newWidgetIds);

        ArrayList<Integer> integers = new ArrayList<Integer>();
        for (int id : newWidgetIds) {
            integers.add(id);
        }
        return integers;
    }

    public View getViewFor(int widgetId) {
        return widgetViews.get(widgetId);
    }

    private static ShadowAppWidgetManager proxyFor(AppWidgetManager appWidgetManager) {
        return ((ShadowAppWidgetManager) ProxyDelegatingHandler.getInstance().proxyFor(appWidgetManager));
    }

}
