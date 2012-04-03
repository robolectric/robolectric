package com.xtremelabs.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Looper;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.tester.android.content.TestSharedPreferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class ShadowContextWrapper extends ShadowContext {
    @RealObject private ContextWrapper realContextWrapper;
    protected Context baseContext;

    private PackageManager packageManager;

    private String appName;
    private String packageName;
    private Set<String> grantedPermissions = new HashSet<String>();

    public void __constructor__(Context baseContext) {
        this.baseContext = baseContext;
    }

    @Implementation
    public Context getApplicationContext() {
        return baseContext.getApplicationContext();
    }

    @Implementation
    public Resources.Theme getTheme() {
        return getResources().newTheme();
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

    public List<Intent> getBroadcastIntents() {
        return ((ShadowApplication) shadowOf(getApplicationContext())).getBroadcastIntents();
    }

    @Implementation
    public int checkPermission(java.lang.String permission, int pid, int uid) {
        return grantedPermissions.contains(permission) ? PERMISSION_GRANTED : PERMISSION_DENIED;
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
        return realContextWrapper == getApplicationContext() ? packageName : getApplicationContext().getPackageName();
    }

    @Implementation
    public ApplicationInfo getApplicationInfo() {
        ApplicationInfo appInfo = new ApplicationInfo();
        appInfo.name = appName;
        appInfo.packageName = packageName;
        appInfo.processName = packageName;
        return appInfo;
    }

    /**
     * Non-Android accessor to set the application name.
     *
     * @param name
     */
    public void setApplicationName(String name) {
        appName = name;
    }

    /**
     * Implements Android's {@code PackageManager}.
     *
     * @return a {@code RobolectricPackageManager}
     */
    @Implementation
    public PackageManager getPackageManager() {
        return realContextWrapper == getApplicationContext() ? packageManager : getApplicationContext().getPackageManager();
    }

    @Implementation
    public ComponentName startService(Intent service) {
        return getApplicationContext().startService(service);
    }

    @Implementation
    public boolean stopService(Intent name) {
        return getApplicationContext().stopService(name);
    }

    @Implementation
    public void startActivity(Intent intent) {
        getApplicationContext().startActivity(intent);
    }

    @Implementation
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new TestSharedPreferences(getShadowApplication().getSharedPreferenceMap(), name, mode);
    }

    @Implementation
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    /**
     * Non-Android accessor that delegates to the application to consume and return the next {@code Intent} on the
     * started activities stack.
     *
     * @return the next started {@code Intent} for an activity
     */
    public Intent getNextStartedActivity() {
        return getShadowApplication().getNextStartedActivity();
    }

    /**
     * Non-Android accessor that delegates to the application to return (without consuming) the next {@code Intent} on
     * the started activities stack.
     *
     * @return the next started {@code Intent} for an activity
     */
    public Intent peekNextStartedActivity() {
        return getShadowApplication().peekNextStartedActivity();
    }

    /**
     * Non-Android accessor that delegates to the application to consume and return the next {@code Intent} on the
     * started services stack.
     *
     * @return the next started {@code Intent} for a service
     */
    public Intent getNextStartedService() {
        return getShadowApplication().getNextStartedService();
    }

    /**
     * Non-android accessor that delefates to the application to clear the stack of started
     * service intents.
     */
    public void clearStartedServices() {
        getShadowApplication().clearStartedServices();
    }

    /**
     * Return (without consuming) the next {@code Intent} on the started services stack.
     *
     * @return the next started {@code Intent} for a service
     */
    public Intent peekNextStartedService() {
        return getShadowApplication().peekNextStartedService();
    }

    /**
     * Non-Android accessor that delegates to the application to return the next {@code Intent} to stop
     * a service (irrespective of if the service was running)
     *
     * @return {@code Intent} for the next service requested to be stopped
     */
    public Intent getNextStoppedService() {
        return getShadowApplication().getNextStoppedService();
    }

    /**
     * Non-Android accessor that is used at start-up to set the package name
     *
     * @param packageName the package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Non-Android accessor that is used at start-up to set the packageManager =
     *
     * @param packageManager the package manager
     */
    public void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }


    @Implementation
    public Looper getMainLooper() {
        return getShadowApplication().getMainLooper();
    }

    @Implementation
    public Context getBaseContext() {
        return baseContext;
    }

    @Implementation
    public void attachBaseContext(Context context) {
        baseContext = context;
    }

    private ShadowApplication getShadowApplication() {
        return ((ShadowApplication) shadowOf(getApplicationContext()));
    }

    @Implementation
    public boolean bindService(Intent intent, final ServiceConnection serviceConnection, int i) {
        return getShadowApplication().bindService(intent, serviceConnection, i);
    }

    /**
     * Non-Android accessor that is used to grant permissions checked via
     * {@link android.content.ContextWrapper#checkPermission(String, int, int)}
     *
     * @param permissionNames permission names that should be granted
     */
    public void grantPermissions(String... permissionNames) {
        for (String permissionName : permissionNames) {
            grantedPermissions.add(permissionName);
        }
    }
}
