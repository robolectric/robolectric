package org.robolectric.shadows;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.ResourceLoader;
import org.robolectric.fakes.RoboSharedPreferences;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shadow for {@link android.content.ContextWrapper}.
 */
@Implements(ContextWrapper.class)
public class ShadowContextWrapper extends ShadowContext {
  private final Map<String, RoboSharedPreferences> sharedPreferencesMap = new HashMap<>();
  @RealObject
  private ContextWrapper realContextWrapper;
  private String packageName;

  @Implementation
  public int checkCallingPermission(String permission) {
    return checkPermission(permission, -1, -1);
  }

  @Implementation
  public int checkCallingOrSelfPermission(String permission) {
    return checkPermission(permission, -1, -1);
  }

  @Implementation
  public Context getApplicationContext() {
    Context applicationContext = realContextWrapper.getBaseContext().getApplicationContext();
    return applicationContext == null ? RuntimeEnvironment.application : applicationContext;
  }

  @Implementation
  public ApplicationInfo getApplicationInfo() {
    try {
      final PackageManager packageManager = RuntimeEnvironment.getPackageManager();
      return packageManager != null ? packageManager.getApplicationInfo(getPackageName(), 0) : null;
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException("Could not find applicationInfo for current package.");
    }
  }

  @Implementation
  public int getUserId() {
    return 0;
  }

  @Override
  public ResourceLoader getResourceLoader() {
    return super.getResourceLoader();
  }

  @Implementation
  @Override
  public String getString(int resId) {
    return super.getString(resId);
  }

  @Implementation
  @Override
  public String getString(int resId, Object... formatArgs) {
    return super.getString(resId, formatArgs);
  }

  @Implementation
  @Override
  public CharSequence getText(int resId) {
    return super.getText(resId);
  }

  @Implementation
  @Override
  public File getExternalCacheDir() {
    return super.getExternalCacheDir();
  }

  @Implementation
  @Override
  public File getExternalFilesDir(String type) {
    return super.getExternalFilesDir(type);
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
  public void sendBroadcast(Intent intent) {
    getApplicationContext().sendBroadcast(intent);
  }

  @Implementation
  public void sendBroadcast(Intent intent, String receiverPermission) {
    getApplicationContext().sendBroadcast(intent, receiverPermission);
  }

  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
    getApplicationContext().sendOrderedBroadcast(intent, receiverPermission);
  }

  @Implementation
  public void sendStickyBroadcast(Intent intent) {
    getApplicationContext().sendStickyBroadcast(intent);
  }

  public List<Intent> getBroadcastIntents() {
    return ((ShadowApplication) Shadows.shadowOf(getApplicationContext())).getBroadcastIntents();
  }

  @Implementation
  public int checkPermission(String permission, int pid, int uid) {
    return getShadowApplication().checkPermission(permission, pid, uid);
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    return ((ShadowApplication) Shadows.shadowOf(getApplicationContext())).registerReceiverWithContext(receiver, filter, null, null, realContextWrapper);
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
    return ((ShadowApplication) Shadows.shadowOf(getApplicationContext())).registerReceiverWithContext(receiver, filter, broadcastPermission, scheduler, realContextWrapper);
  }

  @Implementation
  public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
    getApplicationContext().unregisterReceiver(broadcastReceiver);
  }

  @Implementation
  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  @Implementation
  public String getPackageName() {
    return realContextWrapper == getApplicationContext() ? packageName : getApplicationContext().getPackageName();
  }

  @Implementation
  public PackageManager getPackageManager() {
    return RuntimeEnvironment.getPackageManager();
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
  public void startActivity(Intent intent, Bundle options) {
    getApplicationContext().startActivity(intent, options);
  }

  @Implementation
  public void startActivities(Intent[] intents) {
    for (int i = intents.length - 1; i >= 0; i--) {
      startActivity(intents[i]);
    }
  }

  @Implementation
  public void startActivities(Intent[] intents, Bundle options) {
    for (int i = intents.length - 1; i >= 0; i--) {
      startActivity(intents[i], options);
    }
  }

  @Implementation
  public SharedPreferences getSharedPreferences(String name, int mode) {
    if (!sharedPreferencesMap.containsKey(name)) {
      sharedPreferencesMap.put(name, new RoboSharedPreferences(getShadowApplication().getSharedPreferenceMap(), name, mode));
    }

    return sharedPreferencesMap.get(name);
  }

  @Implementation
  public AssetManager getAssets() {
    return getResources().getAssets();
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

  @Implementation
  public Looper getMainLooper() {
    return getShadowApplication().getMainLooper();
  }

  public ShadowApplication getShadowApplication() {
    return ((ShadowApplication) Shadows.shadowOf(getApplicationContext()));
  }

  @Implementation
  public boolean bindService(Intent intent, final ServiceConnection serviceConnection, int i) {
    return getShadowApplication().bindService(intent, serviceConnection, i);
  }

  @Implementation
  public void unbindService(final ServiceConnection serviceConnection) {
    getShadowApplication().unbindService(serviceConnection);
  }

  @Implementation
  public boolean isRestricted() {
    return false;
  }

  public void grantPermissions(String... permissionNames) {
    getShadowApplication().grantPermissions(permissionNames);
  }

  public void denyPermissions(String... permissionNames) {
    getShadowApplication().denyPermissions(permissionNames);
  }
}
