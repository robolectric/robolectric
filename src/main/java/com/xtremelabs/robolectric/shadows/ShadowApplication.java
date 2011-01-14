package com.xtremelabs.robolectric.shadows;

import android.app.AlarmManager;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.util.Scheduler;
import com.xtremelabs.robolectric.view.TestWindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private ContentResolver contentResolver;
    private AlarmManager alarmManager;
    private LocationManager locationManager;
    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;
    private WindowManager windowManager;
    private AudioManager audioManager;
    private List<Intent> startedActivities = new ArrayList<Intent>();
    private List<Intent> startedServices = new ArrayList<Intent>();
    private List<Wrapper> registeredReceivers = new ArrayList<Wrapper>();
    private FakeHttpLayer fakeHttpLayer = new FakeHttpLayer();
    private final Looper mainLooper = newInstanceOf(Looper.class);
    private Looper currentLooper = mainLooper;
    private Scheduler backgroundScheduler = new Scheduler();
    private Map<String, Hashtable<String, Object>> sharedPreferenceMap = new HashMap<String, Hashtable<String, Object>>();
    private ArrayList<Toast> shownToasts = new ArrayList<Toast>();
    private ShadowAlertDialog latestAlertDialog;
    private ShadowDialog latestDialog;
    private BluetoothAdapter bluetoothAdapter = Robolectric.newInstanceOf(BluetoothAdapter.class);

    // these are managed by the AppSingletonizier... kinda gross, sorry [xw]
    LayoutInflater layoutInflater;
    AppWidgetManager appWidgetManager;

    /**
     * Associates a {@code ResourceLoader} with an {@code Application} instance
     *
     * @param application    application
     * @param resourceLoader resource loader
     * @return the application
     *         todo: make this non-static?
     */
    public static Application bind(Application application, ResourceLoader resourceLoader) {
        ShadowApplication shadowApplication = shadowOf(application);
        if (shadowApplication.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowApplication.resourceLoader = resourceLoader;
        return application;
    }

    public List<Toast> getShownToasts() {
        return shownToasts;
    }

    public Scheduler getBackgroundScheduler() {
        return backgroundScheduler;
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
        if (contentResolver == null) {
            contentResolver = new ContentResolver(realApplication) {
            };
        }
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
        } else if (name.equals(Context.AUDIO_SERVICE)) {
            return audioManager == null ? audioManager = newInstanceOf(AudioManager.class) : audioManager;
        } else if (name.equals(Context.CONNECTIVITY_SERVICE)) {
            return connectivityManager == null ? connectivityManager = newInstanceOf(ConnectivityManager.class) : connectivityManager;
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
     *
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
     *
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
     *
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
     *
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
     *
     * @return the {@code ResourceLoader} associated with this Application
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * Broadcasts the {@code Intent} by iterating through the registered receivers, invoking their filters, and calling
     * {@code onRecieve(Application, Intent)} as appropriate. Does not enqueue the {@code Intent} for later inspection.
     *
     * @param intent the {@code Intent} to broadcast
     *               todo: enqueue the Intent for later inspection
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
     *
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
     *
     * @param context the {@code Context} to check for on each of the remaining registered receivers
     * @param type    the type to report for the context if an exception is thrown
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
     * Non-Android accessor.
     *
     * @return list of {@link Wrapper}s for registered receivers
     */
    public List<Wrapper> getRegisteredReceivers() {
        return registeredReceivers;
    }

    /**
     * Non-Android accessor.
     *
     * @return the layout inflater used by this {@code Application}
     */
    public LayoutInflater getLayoutInflater() {
        return layoutInflater;
    }

    /**
     * Non-Android accessor.
     *
     * @return the app widget manager used by this {@code Application}
     */
    public AppWidgetManager getAppWidgetManager() {
        return appWidgetManager;
    }

    public FakeHttpLayer getFakeHttpLayer() {
        return fakeHttpLayer;
    }

    @Override @Implementation
    public Looper getMainLooper() {
        return mainLooper;
    }

    public Looper getCurrentLooper() {
        return currentLooper;
    }

    public Map<String, Hashtable<String, Object>> getSharedPreferenceMap() {
        return sharedPreferenceMap;
    }

    public ShadowAlertDialog getLatestAlertDialog() {
        return latestAlertDialog;
    }

    public void setLatestAlertDialog(ShadowAlertDialog latestAlertDialog) {
        this.latestAlertDialog = latestAlertDialog;
    }

    public ShadowDialog getLatestDialog() {
        return latestDialog;
    }

    public void setLatestDialog(ShadowDialog latestDialog) {
        this.latestDialog = latestDialog;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public class Wrapper {
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
