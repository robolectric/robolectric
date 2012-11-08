package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.widget.Toast;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.tester.org.apache.http.FakeHttpLayer;
import com.xtremelabs.robolectric.util.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
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
    private static final Map<String, String> SYSTEM_SERVICE_MAP = new HashMap<String, String>();

    static {
        // note that these are different!
    	// They specify concrete classes within Robolectric for interfaces or abstract classes defined by Android
        SYSTEM_SERVICE_MAP.put(Context.WINDOW_SERVICE, "com.xtremelabs.robolectric.tester.android.view.TestWindowManager");
        SYSTEM_SERVICE_MAP.put(Context.CLIPBOARD_SERVICE, "com.xtremelabs.robolectric.tester.android.text.TestClipboardManager");
        SYSTEM_SERVICE_MAP.put(Context.SENSOR_SERVICE, "android.hardware.TestSensorManager");
        SYSTEM_SERVICE_MAP.put(Context.VIBRATOR_SERVICE, "android.os.TestVibrator");
        
        // the rest are as mapped in docs...
        SYSTEM_SERVICE_MAP.put(Context.LAYOUT_INFLATER_SERVICE, "android.view.LayoutInflater");
        SYSTEM_SERVICE_MAP.put(Context.ACTIVITY_SERVICE, "android.app.ActivityManager");
        SYSTEM_SERVICE_MAP.put(Context.POWER_SERVICE, "android.os.PowerManager");
        SYSTEM_SERVICE_MAP.put(Context.ALARM_SERVICE, "android.app.AlarmManager");
        SYSTEM_SERVICE_MAP.put(Context.NOTIFICATION_SERVICE, "android.app.NotificationManager");
        SYSTEM_SERVICE_MAP.put(Context.KEYGUARD_SERVICE, "android.app.KeyguardManager");
        SYSTEM_SERVICE_MAP.put(Context.LOCATION_SERVICE, "android.location.LocationManager");
        SYSTEM_SERVICE_MAP.put(Context.SEARCH_SERVICE, "android.app.SearchManager");
        SYSTEM_SERVICE_MAP.put(Context.STORAGE_SERVICE, "android.os.storage.StorageManager");
        SYSTEM_SERVICE_MAP.put(Context.CONNECTIVITY_SERVICE, "android.net.ConnectivityManager");
        SYSTEM_SERVICE_MAP.put(Context.WIFI_SERVICE, "android.net.wifi.WifiManager");
        SYSTEM_SERVICE_MAP.put(Context.AUDIO_SERVICE, "android.media.AudioManager");
        SYSTEM_SERVICE_MAP.put(Context.TELEPHONY_SERVICE, "android.telephony.TelephonyManager");
        SYSTEM_SERVICE_MAP.put(Context.INPUT_METHOD_SERVICE, "android.view.inputmethod.InputMethodManager");
        SYSTEM_SERVICE_MAP.put(Context.UI_MODE_SERVICE, "android.app.UiModeManager");
        SYSTEM_SERVICE_MAP.put(Context.DOWNLOAD_SERVICE, "android.app.DownloadManager");
    }

    @RealObject private Application realApplication;

    private ResourceLoader resourceLoader;
    private ContentResolver contentResolver;
    private Map<String, Object> systemServices = new HashMap<String, Object>();
    private List<Intent> startedActivities = new ArrayList<Intent>();
    private List<Intent> startedServices = new ArrayList<Intent>();
    private List<Intent> stoppedServies = new ArrayList<Intent>();
    private List<Intent> broadcastIntents = new ArrayList<Intent>();
    private List<ServiceConnection> unboundServiceConnections = new ArrayList<ServiceConnection>();
    private List<Wrapper> registeredReceivers = new ArrayList<Wrapper>();
    private Map<String, Intent> stickyIntents = new HashMap<String, Intent>();
    private FakeHttpLayer fakeHttpLayer = new FakeHttpLayer();
    private Looper mainLooper = ShadowLooper.myLooper();
    private Scheduler backgroundScheduler = new Scheduler();
    private Map<String, Map<String, Object>> sharedPreferenceMap = new HashMap<String, Map<String, Object>>();
    private ArrayList<Toast> shownToasts = new ArrayList<Toast>();
    private PowerManager.WakeLock latestWakeLock;
    private ShadowAlertDialog latestAlertDialog;
    private ShadowDialog latestDialog;
    private Object bluetoothAdapter = Robolectric.newInstanceOf("android.bluetooth.BluetoothAdapter");
    private Resources resources;

    // these are managed by the AppSingletonizier... kinda gross, sorry [xw]
    LayoutInflater layoutInflater;
    AppWidgetManager appWidgetManager;
    private ServiceConnection serviceConnection;
    private ComponentName componentNameForBindService;
    private IBinder serviceForBindService;
    private List<String> unbindableActions = new ArrayList<String>();

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
        shadowApplication.resources = ShadowResources.bind(new Resources(null, null, null), resourceLoader);        
        return application;
    }

    public List<Toast> getShownToasts() {
        return shownToasts;
    }
    
    public Scheduler getBackgroundScheduler() {
        return backgroundScheduler;
    }

    @Override
    @Implementation
    public Context getApplicationContext() {
        return realApplication;
    }

    @Override
    @Implementation
    public Resources getResources() {
        if (resources == null ) {
        	resources = ShadowResources.bind(new Resources(null, null, null), resourceLoader);
        }
        return resources;
    }

    @Implementation
    @Override
    public ContentResolver getContentResolver() {
        if (contentResolver == null) {
            contentResolver = new ContentResolver(realApplication) {
            };
        }
        return contentResolver;
    }

    @Implementation
    @Override
    public Object getSystemService(String name) {
        if (name.equals(Context.LAYOUT_INFLATER_SERVICE)) {
            return LayoutInflater.from(realApplication);
        } else {
            Object service = systemServices.get(name);
            if (service == null) {
                String serviceClassName = SYSTEM_SERVICE_MAP.get(name);
                if (serviceClassName != null) {
                    try {
                        service = newInstanceOf(Class.forName(serviceClassName));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    systemServices.put(name, service);
                }
            }
            return service;
        }
    }
    
    @Implementation
    @Override
    public void startActivity(Intent intent) {
        startedActivities.add(intent);
    }

    @Implementation
    @Override
    public ComponentName startService(Intent intent) {
        startedServices.add(intent);
        return new ComponentName("some.service.package", "SomeServiceName-FIXME");
    }

    @Implementation
    @Override
    public boolean stopService(Intent name) {
        stoppedServies.add(name);

        return startedServices.contains(name);
    }

    public void setComponentNameAndServiceForBindService(ComponentName name, IBinder service) {
        this.componentNameForBindService = name;
        this.serviceForBindService = service;
    }

    @Implementation
    public boolean bindService(Intent intent, final ServiceConnection serviceConnection, int i) {
        if (unbindableActions.contains(intent.getAction())) {
            return false;
        }
        startedServices.add(intent);
        shadowOf(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serviceConnection.onServiceConnected(componentNameForBindService, serviceForBindService);
            }
        }, 0);
        return true;
    }

    @Implementation
    public void unbindService(final ServiceConnection serviceConnection) {
        unboundServiceConnections.add(serviceConnection);
        shadowOf(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                serviceConnection.onServiceDisconnected(componentNameForBindService);
            }
        }, 0);
    }

    public List<ServiceConnection> getUnboundServiceConnections() {
        return unboundServiceConnections;
    }
    /**
     * Consumes the most recent {@code Intent} started by {@link #startActivity(android.content.Intent)} and returns it.
     *
     * @return the most recently started {@code Intent}
     */
    @Override
    public Intent getNextStartedActivity() {
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
    @Override
    public Intent peekNextStartedActivity() {
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
    @Override
    public Intent getNextStartedService() {
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
    @Override
    public Intent peekNextStartedService() {
        if (startedServices.isEmpty()) {
            return null;
        } else {
            return startedServices.get(0);
        }
    }

    /**
     * Clears all {@code Intent} started by {@link #startService(android.content.Intent)}
     */
    @Override
    public void clearStartedServices() {
        startedServices.clear();
    }

    /**
     * Consumes the {@code Intent} requested to stop a service by {@link #stopService(android.content.Intent)}
     * from the bottom of the stack of stop requests.
     */
    @Override
    public Intent getNextStoppedService() {
        if (stoppedServies.isEmpty()) {
            return null;
        } else {
            return stoppedServies.remove(0);
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
    @Override
    @Implementation
    public void sendBroadcast(Intent intent) {
        broadcastIntents.add(intent);
		
        List<Wrapper> copy = new ArrayList<Wrapper>();
        copy.addAll(registeredReceivers);
        for (Wrapper wrapper : copy) {
            if (wrapper.intentFilter.matchAction(intent.getAction())) {
                wrapper.broadcastReceiver.onReceive(realApplication, intent);
            }
        }
    }
	
    public List<Intent> getBroadcastIntents() {
        return broadcastIntents;
    }

    @Implementation
    public void sendStickyBroadcast(Intent intent) {
        stickyIntents.put(intent.getAction(), intent);
        sendBroadcast(intent);
    }

    /**
     * Always returns {@code null}
     *
     * @return {@code null}
     */
    @Override
    @Implementation
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiverWithContext(receiver, filter, realApplication);
    }

    Intent registerReceiverWithContext(BroadcastReceiver receiver, IntentFilter filter, Context context) {
        if (receiver != null) {
            registeredReceivers.add(new Wrapper(receiver, filter, context));
        }
        return getStickyIntent(filter);
    }

    private Intent getStickyIntent(IntentFilter filter) {
        for (Intent stickyIntent : stickyIntents.values()) {
            String action = null;
            for (int i = 0; i < filter.countActions(); i++) {
                action = filter.getAction(i);
                if (stickyIntent.getAction().equals(action)) {
                    return stickyIntent;
                }
            }
        }

        return null;
    }

    @Override
    @Implementation
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

    public void assertNoBroadcastListenersOfActionRegistered(Context context, String action) {
        for (Wrapper registeredReceiver : registeredReceivers) {
            if (registeredReceiver.context == context) {
                Iterator<String> actions = registeredReceiver.intentFilter.actionsIterator();
                while (actions.hasNext()) {
                    if (actions.next().equals(action)) {
                        RuntimeException e = new IllegalStateException("Unexpected BroadcastReceiver on " + context +
                                " with action " + action + " "
                                + registeredReceiver.broadcastReceiver + " that was originally registered here:");
                        e.setStackTrace(registeredReceiver.exception.getStackTrace());
                        throw e;
                    }
                }
            }
        }
    }

    public boolean hasReceiverForIntent(Intent intent) {
        for (Wrapper wrapper : registeredReceivers) {
            if (wrapper.intentFilter.matchAction(intent.getAction())) {
                return true;
            }
        }
        return false;
    }

    public List<BroadcastReceiver> getReceiversForIntent(Intent intent) {
        ArrayList<BroadcastReceiver> broadcastReceivers = new ArrayList<BroadcastReceiver>();
        for (Wrapper wrapper : registeredReceivers) {
            if (wrapper.intentFilter.matchAction(intent.getAction())) {
                broadcastReceivers.add(wrapper.getBroadcastReceiver());
            }
        }
        return broadcastReceivers;
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

    @Override
    @Implementation
    public Looper getMainLooper() {
        return mainLooper;
    }

    public Map<String, Map<String, Object>> getSharedPreferenceMap() {
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

    public Object getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void declareActionUnbindable(String action) {
        unbindableActions.add(action);
    }

    public void setSystemService(String key, Object service) {
        systemServices.put(key, service);
    }
    
    public PowerManager.WakeLock getLatestWakeLock() {
    	return latestWakeLock;
    }
    
    public void addWakeLock( PowerManager.WakeLock wl ) {
    	latestWakeLock = wl;
    }
    
    public void clearWakeLocks() {
    	latestWakeLock = null;
    }

    public class Wrapper {
        public BroadcastReceiver broadcastReceiver;
        public IntentFilter intentFilter;
        public Context context;
        public Throwable exception;

        public Wrapper(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, Context context) {
            this.broadcastReceiver = broadcastReceiver;
            this.intentFilter = intentFilter;
            this.context = context;
            exception = new Throwable();
        }

        public BroadcastReceiver getBroadcastReceiver() {
            return broadcastReceiver;
        }

        public IntentFilter getIntentFilter() {
            return intentFilter;
        }

        public Context getContext() {
            return context;
        }
    }
}
