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
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.ResourceLoader;
import org.robolectric.tester.android.content.TestSharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CursorFactory;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class ShadowContextWrapper extends ShadowContext {
  @RealObject private ContextWrapper realContextWrapper;

  private String appName;
  private String packageName;

  @Implementation
  public int checkCallingPermission(String permission) {
    return checkPermission(permission, -1 , -1);
  }

  @Implementation
  public int checkCallingOrSelfPermission(String permission) {
    return checkPermission(permission, -1, -1);
  }

  @Implementation
  public Context getApplicationContext() {
    Context applicationContext = realContextWrapper.getBaseContext().getApplicationContext();
    return applicationContext == null ? Robolectric.application : applicationContext;
  }

  @Implementation
  @Override public File getFilesDir() {
    return super.getFilesDir();
  }

  @Implementation
  @Override public File getCacheDir() {
    return super.getCacheDir();
  }

  @Implementation
  @Override public String[] fileList() {
    return super.fileList();
  }

  @Implementation
  @Override public File getDatabasePath(String name) {
    return super.getDatabasePath(name);
  }

  @Implementation
  @Override public File getFileStreamPath(String name) {
    return super.getFileStreamPath(name);
  }

  @Override public ResourceLoader getResourceLoader() {
    return super.getResourceLoader();
  }

  @Implementation
  @Override public String getString(int resId) {
    return super.getString(resId);
  }

  @Implementation
  @Override public String getString(int resId, Object... formatArgs) {
    return super.getString(resId, formatArgs);
  }

  @Implementation
  @Override public CharSequence getText(int resId) {
    return super.getText(resId);
  }

  @Implementation
  @Override public File getExternalCacheDir() {
    return super.getExternalCacheDir();
  }

  @Implementation
  @Override public File getExternalFilesDir(String type) {
    return super.getExternalFilesDir(type);
  }

  @Implementation
  @Override public FileInputStream openFileInput(String path) throws FileNotFoundException {
    return super.openFileInput(path);
  }

  @Implementation
  @Override public FileOutputStream openFileOutput(String path, int mode) throws FileNotFoundException {
    return super.openFileOutput(path, mode);
  }

  @Implementation
  @Override public boolean deleteFile(String name) {
    return super.deleteFile(name);
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

  public List<Intent> getBroadcastIntents() {
    return ((ShadowApplication) shadowOf(getApplicationContext())).getBroadcastIntents();
  }

  @Implementation
  public int checkPermission(String permission, int pid, int uid) {
    return getShadowApplication().checkPermission(permission, pid, uid);
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    return ((ShadowApplication) shadowOf(getApplicationContext())).registerReceiverWithContext(receiver, filter, null, null, realContextWrapper);
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
    return ((ShadowApplication) shadowOf(getApplicationContext())).registerReceiverWithContext(receiver, filter, broadcastPermission, scheduler, realContextWrapper);
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
    return Robolectric.packageManager;
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

  @Implementation
  public Looper getMainLooper() {
    return getShadowApplication().getMainLooper();
  }

  public ShadowApplication getShadowApplication() {
    return ((ShadowApplication) shadowOf(getApplicationContext()));
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

  @Implementation
  public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
    return SQLiteDatabase.openDatabase(name, factory, 0);
  }
}
