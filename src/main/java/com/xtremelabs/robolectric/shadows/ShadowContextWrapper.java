package com.xtremelabs.robolectric.shadows;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Looper;
import com.xtremelabs.robolectric.content.TestSharedPreferences;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.res.RobolectricPackageManager;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class ShadowContextWrapper extends ShadowContext {
    @RealObject private ContextWrapper realContextWrapper;
    private Context baseContext;

    private PackageManager packageManager;

    private String packageName;

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

    /**
     * Implements Android's {@code PackageManager}.
     *
     * @return a {@code RobolectricPackageManager}
     */
    @Implementation
    public PackageManager getPackageManager() {
        if (packageManager == null) {
            packageManager = new RobolectricPackageManager(realContextWrapper);
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
     * Return (without consuming) the next {@code Intent} on the started services stack.
     *
     * @return the next started {@code Intent} for a service
     */
    public Intent peekNextStartedService() {
        return getShadowApplication().peekNextStartedService();
    }

    /**
     * Non-Android accessor that is used at start-up to set the package name
     *
     * @param packageName the package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Implementation
    public Looper getMainLooper() {
        return getShadowApplication().getMainLooper();
    }

    private ShadowApplication getShadowApplication() {
        return ((ShadowApplication) shadowOf(getApplicationContext()));
    }

}
