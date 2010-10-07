package com.xtremelabs.droidsugar.fakes;

import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.test.mock.MockPackageManager;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implements;
import com.xtremelabs.droidsugar.util.ResourceLoader;
import com.xtremelabs.droidsugar.view.TestSharedPreferences;

import java.util.*;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class FakeContextWrapper {
    public static ResourceLoader resourceLoader;

    private ContextWrapper realContextWrapper;

    public List<Intent> startedServices = new ArrayList<Intent>();
    private LocationManager locationManager;
    private MockPackageManager packageManager;

    public Map<String, BroadcastReceiver> registeredReceivers = new HashMap<String, BroadcastReceiver>();
    private WifiManager wifiManager;

    public FakeContextWrapper(ContextWrapper realContextWrapper) {
        this.realContextWrapper = realContextWrapper;
    }

    public Resources getResources() {
        return new Resources(null, null, null);
    }

    public Context getApplicationContext() {
        return FakeHelper.application;
    }

    public ContentResolver getContentResolver() {
        return getApplicationContext().getContentResolver();
    }

    public Object getSystemService(String name) {
        return getApplicationContext().getSystemService(name);
    }

    public void sendBroadcast(Intent intent) {
        BroadcastReceiver broadcastReceiver = registeredReceivers.get(intent.getAction());
        if (broadcastReceiver != null) {
            broadcastReceiver.onReceive(realContextWrapper, intent);
        }
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        Iterator<String> iterator = filter.actionsIterator();
        while (iterator.hasNext()) {
            String action = iterator.next();
            registeredReceivers.put(action, receiver);
        }
        return null;
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        Iterator<Map.Entry<String, BroadcastReceiver>> entryIterator = registeredReceivers.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, BroadcastReceiver> stringBroadcastReceiverEntry = entryIterator.next();
            if (stringBroadcastReceiverEntry.getValue() == receiver) {
                entryIterator.remove();
            }
        }
    }

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

    public ComponentName startService(Intent service) {
        startedServices.add(service);
        return new ComponentName("some.service.package", "SomeServiceName");
    }

    public void startActivity(Intent intent) {
        getApplicationContext().startActivity(intent);
    }

    public Intent getNextStartedIntent() {
        return ((FakeApplication) ProxyDelegatingHandler.getInstance().proxyFor(getApplicationContext())).getNextStartedIntent();
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new TestSharedPreferences(name, mode);
    }
}
