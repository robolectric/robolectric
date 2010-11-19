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

/**
 * Shadows the {@code android.app.Application} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Application.class)
public class ShadowApplication extends ShadowContextWrapper {
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

    /**
     * Associates a {@code ResourceLoader} with an {@code Application} instance
     * @param application
     * @param resourceLoader
     * @return the application
     * todo: make this non-static?
     */
    public static Application bind(Application application, ResourceLoader resourceLoader) {
        ShadowApplication shadowApplication = (ShadowApplication) shadowOf(application);
        if (shadowApplication.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowApplication.resourceLoader = resourceLoader;
        return application;
    }

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

    /**
     * Consumes the most recent {@code Intent} started by {@link #startActivity(android.content.Intent)} and returns it.
     * @return the most recently started {@code Intent}
     */
    @Override public Intent getNextStartedActivity() {
        if (startedActivities.isEmpty()) {
            return null;
        } else {
            return startedActivities.remove(0);
        }
    }

    /**
     * Returns the most recent {@code Intent} started by {@link #startActivity(android.content.Intent)} without
     * consuming it.
     * @return the most recently started {@code Intent}
     */
    @Override public Intent peekNextStartedActivity() {
        if (startedActivities.isEmpty()) {
            return null;
        } else {
            return startedActivities.get(0);
        }
    }

    /**
     * Consumes the most recent {@code Intent} started by {@link #startService(android.content.Intent)} and returns it.
     * @return the most recently started {@code Intent}
     */
    @Override public Intent getNextStartedService() {
        if (startedServices.isEmpty()) {
            return null;
        } else {
            return startedServices.remove(0);
        }
    }

    /**
     * Returns the most recent {@code Intent} started by {@link #startService(android.content.Intent)} without
     * consuming it.
     * @return the most recently started {@code Intent}
     */
    @Override public Intent peekNextStartedService() {
        if (startedServices.isEmpty()) {
            return null;
        } else {
            return startedServices.get(0);
        }
    }

    /**
     * Non-Android accessor (and a handy way to get a working {@code ResourceLoader}
     * @return the {@code ResourceLoader} associated with this Application
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * Broadcasts the {@code Intent} by iterating through the registered receivers, invoking their filters, and calling
     * {@code onRecieve(Application, Intent)} as appropriate. Does not enqueue the {@code Intent} for later inspection.
     * @param intent the {@code Intent} to broadcast
     * todo: enqueue the Intent for later inspection
     */
    @Override @Implementation
    public void sendBroadcast(Intent intent) {
        for (Wrapper wrapper : registeredReceivers) {
            if (wrapper.intentFilter.matchAction(intent.getAction())) {
                wrapper.broadcastReceiver.onReceive(realApplication, intent);
            }
        }
    }

    /**
     * Always returns {@code null}
     * @return {@code null}
     */
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

    /**
     * Iterates through all of the registered receivers on this {@code Application} and if any of them match the given
     * {@code Context} object throws a {@code RuntimeException}
     * @param context the {@code Context} to check for on each of the remaining registered receivers
     * @param type the type to report for the context if an exception is thrown
     * @throws RuntimeException if there are any recievers registered with the given {@code Context}
     */
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

    /**
     * Non-Android accessor
     */
    public List<Wrapper> getRegisteredReceivers() {
        return registeredReceivers;
    }

    /**
     * Non-Android accessor
     */
    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    /**
     * Non-Android accessor
     */
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
