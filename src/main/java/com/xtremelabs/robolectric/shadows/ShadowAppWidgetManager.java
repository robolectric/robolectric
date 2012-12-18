package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;
import com.xtremelabs.robolectric.internal.AppSingletonizer;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.*;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AppWidgetManager.class)
public class ShadowAppWidgetManager {
    private static AppSingletonizer<AppWidgetManager> instances = new AppSingletonizer<AppWidgetManager>(AppWidgetManager.class) {
        @Override
        protected AppWidgetManager get(ShadowApplication shadowApplication) {
            return shadowApplication.appWidgetManager;
        }

        @Override
        protected void set(ShadowApplication shadowApplication, AppWidgetManager instance) {
            shadowApplication.appWidgetManager = instance;
        }

        @Override
        protected AppWidgetManager createInstance(Application applicationContext) {
            AppWidgetManager appWidgetManager = super.createInstance(applicationContext);
            shadowOf(appWidgetManager).context = applicationContext;
            return appWidgetManager;
        }
    };

    @RealObject
    private AppWidgetManager realAppWidgetManager;

    private Context context;
    private Map<Integer, WidgetInfo> widgetInfos = new HashMap<Integer, WidgetInfo>();
    private int nextWidgetId = 1;
    private boolean alwaysRecreateViewsDuringUpdate = false;
    private Map<Integer, AppWidgetProviderInfo> appWidgetProviderInfoForId = new TreeMap<Integer, AppWidgetProviderInfo>();
    private boolean allowedToBindWidgets;
    private boolean validWidgetProviderComponentName = true;

    private static void bind(AppWidgetManager appWidgetManager, Context context) {
        // todo: implement
    }


    /**
     * Finds or creates an {@code AppWidgetManager} for the given {@code context}
     *
     * @param context the {@code context} for which to produce an assoicated {@code AppWidgetManager}
     * @return the {@code AppWidgetManager} associated with the given {@code context}
     */
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

    /**
     * Simulates updating an {@code AppWidget} with a new set of views
     *
     * @param appWidgetId id of widget
     * @param views       views to update
     */
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

    @Implementation
    public int[] getAppWidgetIds(ComponentName provider) {
        List<Integer> idList = new ArrayList<Integer>();
        for (int id : widgetInfos.keySet()) {
            WidgetInfo widgetInfo = widgetInfos.get(id);
            String widgetClass = widgetInfo.appWidgetProvider.getClass().getName();
            String widgetPackage = widgetInfo.appWidgetProvider.getClass().getPackage().getName();
            if (provider.getClassName().equals(widgetClass) && provider.getPackageName().equals(widgetPackage)) {
                idList.add(id);
            }
        }
        int ids[] = new int[idList.size()];
        for (int i = 0; i < idList.size(); i++) {
            ids[i] = idList.get(i);
        }
        return ids;
    }

    @Implementation
    public List<AppWidgetProviderInfo> getInstalledProviders() {
        List<AppWidgetProviderInfo> result = new ArrayList<AppWidgetProviderInfo>();
        for (AppWidgetProviderInfo appWidgetProviderInfo : appWidgetProviderInfoForId.values()) {
            result.add(appWidgetProviderInfo);
        }
        return result;
    }

    public void putWidgetInfo(int appWidgetId, AppWidgetProviderInfo expectedWidgetInfo) {
        this.appWidgetProviderInfoForId.put(appWidgetId, expectedWidgetInfo);
    }

    @Implementation
    public AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
        return appWidgetProviderInfoForId.get(appWidgetId);
    }

    @Implementation
    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, ComponentName provider) {
        if(validWidgetProviderComponentName) {
            return allowedToBindWidgets;
        } else {
            throw new IllegalArgumentException("not an appwidget provider");
        }
    }

    /**
     * Triggers a reapplication of the most recent set of actions against the widget, which is what happens when the
     * phone is rotated. Does not attempt to simulate a change in screen geometry.
     *
     * @param appWidgetId the ID of the widget to be affected
     */
    public void reconstructWidgetViewAsIfPhoneWasRotated(int appWidgetId) {
        WidgetInfo widgetInfo = getWidgetInfo(appWidgetId);
        widgetInfo.view = createWidgetView(widgetInfo.layoutId);
        widgetInfo.lastRemoteViews.reapply(context, widgetInfo.view);
    }

    /**
     * Creates a widget by inflating its layout.
     *
     * @param appWidgetProviderClass the app widget provider class
     * @param widgetLayoutId         id of the layout to inflate
     * @return the ID of the new widget
     */
    public int createWidget(Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId) {
        return createWidgets(appWidgetProviderClass, widgetLayoutId, 1)[0];
    }

    /**
     * Creates a bunch of widgets by inflating the same layout multiple times.
     *
     * @param appWidgetProviderClass the app widget provider class
     * @param widgetLayoutId         id of the layout to inflate
     * @param howManyToCreate        number of new widgets to create
     * @return the IDs of the new widgets
     */
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

    /**
     * Non-Android accessor.
     *
     * @param widgetId id of the desired widget
     * @return the widget associated with {@code widgetId}
     */
    public View getViewFor(int widgetId) {
        return getWidgetInfo(widgetId).view;
    }

    /**
     * Non-Android accessor.
     *
     * @param widgetId id of the widget whose provider is to be returned
     * @return the {@code AppWidgetProvider} associated with {@code widgetId}
     */
    public AppWidgetProvider getAppWidgetProviderFor(int widgetId) {
        return getWidgetInfo(widgetId).appWidgetProvider;
    }

    /**
     * Non-Android mechanism that enables testing of widget behavior when all of the views are recreated on every
     * update. This is useful for ensuring that your widget will behave correctly even if it is restarted by the OS
     * between events.
     *
     * @param alwaysRecreate whether or not to always recreate the views
     */
    public void setAlwaysRecreateViewsDuringUpdate(boolean alwaysRecreate) {
        alwaysRecreateViewsDuringUpdate = alwaysRecreate;
    }

    /**
     * Non-Android accessor.
     *
     * @return the state of the{@code alwaysRecreateViewsDuringUpdate} flag
     */
    public boolean getAlwaysRecreateViewsDuringUpdate() {
        return alwaysRecreateViewsDuringUpdate;
    }

    private WidgetInfo getWidgetInfo(int widgetId) {
        return widgetInfos.get(widgetId);
    }

    public void setAllowedToBindAppWidgets(boolean allowed) {
        allowedToBindWidgets = allowed;
    }

    public void setValidWidgetProviderComponentName(boolean validWidgetProviderComponentName) {
        this.validWidgetProviderComponentName = validWidgetProviderComponentName;
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
