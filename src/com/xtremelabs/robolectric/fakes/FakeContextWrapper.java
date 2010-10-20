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
import com.xtremelabs.robolectric.util.SheepWrangler;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class FakeContextWrapper extends FakeContext {
    @SheepWrangler private ProxyDelegatingHandler proxyDelegatingHandler;
    private ContextWrapper realContextWrapper;
    private Context baseContext;

    private LocationManager locationManager;
    private MockPackageManager packageManager;

    public List<Wrapper> registeredReceivers = new ArrayList<Wrapper>();
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
        for (Wrapper wrapper : registeredReceivers) {
            if (wrapper.intentFilter.matchAction(intent.getAction())) {
                wrapper.broadcastReceiver.onReceive(realContextWrapper, intent);
            }
        }
    }

    @Implementation
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        registeredReceivers.add(new Wrapper(receiver, filter));
        return null;
    }

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
        return ((FakeApplication) proxyDelegatingHandler.proxyFor(getApplicationContext()));
    }

    @Implementation
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new TestSharedPreferences(name, mode);
    }

    private class Wrapper {
        private BroadcastReceiver broadcastReceiver;
        private IntentFilter intentFilter;

        public Wrapper(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
            this.broadcastReceiver = broadcastReceiver;
            this.intentFilter = intentFilter;
        }
    }
}
