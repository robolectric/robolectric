package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import com.xtremelabs.robolectric.util.AppSingletonizer;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.HashMap;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AppWidgetManager.class)
public class ShadowAppWidgetManager {
    private static AppSingletonizer<AppWidgetManager> instances = new AppSingletonizer<AppWidgetManager>(AppWidgetManager.class) {
        @Override protected AppWidgetManager get(ShadowApplication shadowApplication) {
            return shadowApplication.appWidgetManager;
        }

        @Override protected void set(ShadowApplication shadowApplication, AppWidgetManager instance) {
            shadowApplication.appWidgetManager = instance;
        }

        @Override
        protected AppWidgetManager createInstance(Application applicationContext) {
            AppWidgetManager appWidgetManager = super.createInstance(applicationContext);
            shadowOf(appWidgetManager).context = applicationContext;
            return appWidgetManager;
        }
    };

    private static void bind(AppWidgetManager appWidgetManager, Context context) {
    }

    private AppWidgetManager realAppWidgetManager;
    private Context context;
    private Map<Integer, WidgetInfo> widgetInfos = new HashMap<Integer, WidgetInfo>();
    private int nextWidgetId = 1;
    public boolean alwaysRecreateViewsDuringUpdate = false;

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
        WidgetInfo widgetInfo = getWidgetInfo(appWidgetId);
        int layoutId = views.getLayoutId();
        if (widgetInfo.layoutId != layoutId || alwaysRecreateViewsDuringUpdate) {
            widgetInfo.view = createWidgetView(layoutId);
            widgetInfo.layoutId = layoutId;
        }
        widgetInfo.lastRemoteViews = views;
        views.reapply(context, widgetInfo.view);
    }

    public void reconstructWidgetViewAsIfPhoneWasRotated(int appWidgetId) {
        WidgetInfo widgetInfo = getWidgetInfo(appWidgetId);
        widgetInfo.view = createWidgetView(widgetInfo.layoutId);
        widgetInfo.lastRemoteViews.reapply(context, widgetInfo.view);

    }

    public int createWidget(Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId) {
        return createWidgets(appWidgetProviderClass, widgetLayoutId, 1)[0];
    }

    public int[] createWidgets(Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId, int howManyToCreate) {
        AppWidgetProvider appWidgetProvider = newInstanceOf(appWidgetProviderClass);

        int[] newWidgetIds = new int[howManyToCreate];
        for (int i = 0; i < howManyToCreate; i++) {
            View widgetView = createWidgetView(widgetLayoutId);

            int myWidgetId = nextWidgetId++;
            widgetInfos.put(myWidgetId, new WidgetInfo(widgetView, widgetLayoutId, appWidgetProvider));
            newWidgetIds[i] = myWidgetId;
        }

        appWidgetProvider.onUpdate(context, realAppWidgetManager, newWidgetIds);
        return newWidgetIds;
    }

    private void createWidgetProvider(Class<? extends AppWidgetProvider> appWidgetProviderClass, int... newWidgetIds) {
        AppWidgetProvider appWidgetProvider = newInstanceOf(appWidgetProviderClass);
        appWidgetProvider.onUpdate(context, realAppWidgetManager, newWidgetIds);
    }

    private View createWidgetView(int widgetLayoutId) {
        return new Activity().getLayoutInflater().inflate(widgetLayoutId, null);
    }

    public View getViewFor(int widgetId) {
        return getWidgetInfo(widgetId).view;
    }

    public AppWidgetProvider getAppWidgetProviderFor(int widgetId) {
        return getWidgetInfo(widgetId).appWidgetProvider;
    }

    private WidgetInfo getWidgetInfo(int widgetId) {
        return widgetInfos.get(widgetId);
    }

    private class WidgetInfo {
        private View view;
        private int layoutId;
        private AppWidgetProvider appWidgetProvider;
        private RemoteViews lastRemoteViews;

        public WidgetInfo(View view, int layoutId, AppWidgetProvider appWidgetProvider) {
            this.view = view;
            this.layoutId = layoutId;
            this.appWidgetProvider = appWidgetProvider;
        }
    }
}
