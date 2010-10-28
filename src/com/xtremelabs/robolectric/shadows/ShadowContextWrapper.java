package com.xtremelabs.robolectric.shadows;

import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.test.mock.MockPackageManager;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class ShadowContextWrapper extends ShadowContext {
    @RealObject private ContextWrapper realContextWrapper;
    private Context baseContext;

    private LocationManager locationManager;
    private MockPackageManager packageManager;

    private WifiManager wifiManager;

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
        getApplicationContext().sendBroadcast(intent);
    }

    @Implementation
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return ((ShadowApplication) shadowOf(getApplicationContext())).registerReceiverWithContext(receiver, filter, realContextWrapper);
    }

    @Implementation
    public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        getApplicationContext().unregisterReceiver(broadcastReceiver);
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
        return getShadowApplication().getNextStartedActivity();
    }

    public Intent peekNextStartedActivity() {
        return getShadowApplication().peekNextStartedActivity();
    }

    public Intent getNextStartedService() {
        return getShadowApplication().getNextStartedService();
    }

    public Intent peekNextStartedService() {
        return getShadowApplication().peekNextStartedService();
    }

    private ShadowApplication getShadowApplication() {
        return ((ShadowApplication) shadowOf(getApplicationContext()));
    }

    @Implementation
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new TestSharedPreferences(name, mode);
    }
}
