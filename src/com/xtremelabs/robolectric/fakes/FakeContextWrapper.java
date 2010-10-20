package com.xtremelabs.robolectric.fakes;

import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.test.mock.MockPackageManager;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class FakeContextWrapper extends FakeContext {
    private ContextWrapper realContextWrapper;
    private Context baseContext;

    private LocationManager locationManager;
    private MockPackageManager packageManager;

    public Map<String, BroadcastReceiver> registeredReceivers = new HashMap<String, BroadcastReceiver>();
    private WifiManager wifiManager;

    public FakeContextWrapper(ContextWrapper realContextWrapper) {
        super(realContextWrapper);
        this.realContextWrapper = realContextWrapper;
    }

    public void __constructor__(Context baseContext) {
        this.baseContext = baseContext;
    }

    @Implementation
    public Context getApplicationContext() {
        return baseContext.getApplicationContext();
    }

    @Implementation
    public Resources getResources() {
        return getApplicationContext().getResources();
    }

    @Implementation
    public ContentResolver getContentResolver() {
        return getApplicationContext().getContentResolver();
    }

    @Implementation
    public Object getSystemService(String name) {
        return getApplicationContext().getSystemService(name);
    }

    @Implementation
    public void sendBroadcast(Intent intent) {
        BroadcastReceiver broadcastReceiver = registeredReceivers.get(intent.getAction());
        if (broadcastReceiver != null) {
            broadcastReceiver.onReceive(realContextWrapper, intent);
        }
    }

    @Implementation
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        Iterator<String> iterator = filter.actionsIterator();
        while (iterator.hasNext()) {
            String action = iterator.next();
            registeredReceivers.put(action, receiver);
        }
        return null;
    }

    @Implementation
    public void unregisterReceiver(BroadcastReceiver receiver) {
        boolean found = false;
        Iterator<Map.Entry<String, BroadcastReceiver>> entryIterator = registeredReceivers.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, BroadcastReceiver> stringBroadcastReceiverEntry = entryIterator.next();
            if (stringBroadcastReceiverEntry.getValue() == receiver) {
                entryIterator.remove();
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Receiver not registered: " + receiver);
        }
    }

    @Implementation
    public String getPackageName() {
        return "some.package.name";
    }

    @Implementation
    public PackageManager getPackageManager() {
        if (packageManager == null) {
            packageManager = new MockPackageManager() {
                public PackageInfo packageInfo;

                @Override
                public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
                    if (packageInfo == null) {
                        packageInfo = new PackageInfo();
                        packageInfo.versionName = "1.0";
                    }
                    return packageInfo;
                }
            };
        }
        return packageManager;
    }

    @Implementation
    public ComponentName startService(Intent service) {
        return getApplicationContext().startService(service);
    }

    @Implementation
    public void startActivity(Intent intent) {
        getApplicationContext().startActivity(intent);
    }

    public Intent getNextStartedActivity() {
        return getFakeApplication().getNextStartedActivity();
    }

    public Intent peekNextStartedActivity() {
        return getFakeApplication().peekNextStartedActivity();
    }

    public Intent getNextStartedService() {
        return getFakeApplication().getNextStartedService();
    }

    public Intent peekNextStartedService() {
        return getFakeApplication().peekNextStartedService();
    }

    private FakeApplication getFakeApplication() {
        return ((FakeApplication) ProxyDelegatingHandler.getInstance().proxyFor(getApplicationContext()));
    }

    @Implementation
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new TestSharedPreferences(name, mode);
    }
}
