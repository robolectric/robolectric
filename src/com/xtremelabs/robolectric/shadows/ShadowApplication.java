package com.xtremelabs.robolectric.shadows;

import android.app.AlarmManager;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.test.mock.MockContentResolver;
import android.view.LayoutInflater;
import android.view.WindowManager;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.view.TestWindowManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Application.class)
public class ShadowApplication extends ShadowContextWrapper {
    public static Application bind(Application application, ResourceLoader resourceLoader) {
        ShadowApplication shadowApplication = (ShadowApplication) shadowOf(application);
        if (shadowApplication.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowApplication.resourceLoader = resourceLoader;
        return application;
    }

    @RealObject private Application realApplication;

    private ResourceLoader resourceLoader;
    private MockContentResolver contentResolver = new MockContentResolver();
    private AlarmManager alarmManager;
    private LocationManager locationManager;
    private WifiManager wifiManager;
    private WindowManager windowManager;
    private List<Intent> startedActivities = new ArrayList<Intent>();
    private List<Intent> startedServices = new ArrayList<Intent>();
    private List<Wrapper> registeredReceivers = new ArrayList<Wrapper>();

    // these are managed by the AppSingletonizier... kinda gross, sorry [xw]
    LayoutInflater layoutInflater;
    AppWidgetManager appWidgetManager;

    @Override @Implementation
    public Context getApplicationContext() {
        return realApplication;
    }

    @Override @Implementation
    public Resources getResources() {
        return ShadowResources.bind(new Resources(null, null, null), resourceLoader);
    }

    @Implementation
    @Override public ContentResolver getContentResolver() {
        return contentResolver;
    }

    @Implementation
    @Override public Object getSystemService(String name) {
        if (name.equals(Context.LAYOUT_INFLATER_SERVICE)) {
            return LayoutInflater.from(realApplication);
        } else if (name.equals(Context.ALARM_SERVICE)) {
            return alarmManager == null ? alarmManager = newInstanceOf(AlarmManager.class) : alarmManager;
        } else if (name.equals(Context.LOCATION_SERVICE)) {
            return locationManager == null ? locationManager = newInstanceOf(LocationManager.class) : locationManager;
        } else if (name.equals(Context.WIFI_SERVICE)) {
            return wifiManager == null ? wifiManager = newInstanceOf(WifiManager.class) : wifiManager;
        } else if (name.equals(Context.WINDOW_SERVICE)) {
            return windowManager == null ? windowManager = new TestWindowManager() : windowManager;
        }
        return null;
    }

    @Implementation
    @Override public void startActivity(Intent intent) {
        startedActivities.add(intent);
    }

    @Implementation
    @Override public ComponentName startService(Intent intent) {
        startedServices.add(intent);
        return new ComponentName("some.service.package", "SomeServiceName-FIXME");
    }

    @Override public Intent getNextStartedActivity() {
        if (startedActivities.isEmpty()) {
            return null;
        } else {
            return startedActivities.remove(0);
        }
    }

    @Override public Intent peekNextStartedActivity() {
        if (startedActivities.isEmpty()) {
            return null;
        } else {
            return startedActivities.get(0);
        }
    }

    @Override public Intent getNextStartedService() {
        if (startedServices.isEmpty()) {
            return null;
        } else {
            return startedServices.remove(0);
        }
    }

    @Override public Intent peekNextStartedService() {
        if (startedServices.isEmpty()) {
            return null;
        } else {
            return startedServices.get(0);
        }
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override @Implementation
    public void sendBroadcast(Intent intent) {
        for (Wrapper wrapper : registeredReceivers) {
            if (wrapper.intentFilter.matchAction(intent.getAction())) {
                wrapper.broadcastReceiver.onReceive(realApplication, intent);
            }
        }
    }

    @Override @Implementation
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiverWithContext(receiver, filter, realApplication);
    }

    Intent registerReceiverWithContext(BroadcastReceiver receiver, IntentFilter filter, Context context) {
        registeredReceivers.add(new Wrapper(receiver, filter, context));
        return null;
    }

    @Override @Implementation
    public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        boolean found = false;
        Iterator<Wrapper> iterator = registeredReceivers.iterator();
        while (iterator.hasNext()) {
            Wrapper wrapper = iterator.next();
            if (wrapper.broadcastReceiver == broadcastReceiver) {
                iterator.remove();
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Receiver not registered: " + broadcastReceiver);
        }
    }

    public void assertNoBroadcastListenersRegistered(Context context, String type) {
        for (Wrapper registeredReceiver : registeredReceivers) {
            if (registeredReceiver.context == context) {
                RuntimeException e = new IllegalStateException(type + " " + context + " leaked has leaked IntentReceiver "
                        + registeredReceiver.broadcastReceiver + " that was originally registered here. " +
                        "Are you missing a call to unregisterReceiver()?");
                e.setStackTrace(registeredReceiver.exception.getStackTrace());
                throw e;
            }
        }
    }

    public List<Wrapper> getRegisteredReceivers() {
        return registeredReceivers;
    }

    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    public AppWidgetManager getAppWidgetManager() {
        return appWidgetManager;
    }

    private class Wrapper {
        private BroadcastReceiver broadcastReceiver;
        private IntentFilter intentFilter;
        private Context context;
        public Throwable exception;

        public Wrapper(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, Context context) {
            this.broadcastReceiver = broadcastReceiver;
            this.intentFilter = intentFilter;
            this.context = context;
            exception = new Throwable();
        }
    }
}
