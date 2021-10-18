package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.UserHandle;
import com.google.common.base.Strings;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(className = ShadowContextImpl.CLASS_NAME)
@SuppressWarnings("NewApi")
public class ShadowContextImpl {

  public static final String CLASS_NAME = "android.app.ContextImpl";
  private ContentResolver contentResolver;

  @RealObject private Context realContextImpl;

  private Map<String, Object> systemServices = new HashMap<String, Object>();
  private final Set<String> removedSystemServices = new HashSet<>();
  private Integer userId;

  /**
   * Returns the handle to a system-level service by name. If the service is not available in
   * Roboletric, or it is set to unavailable in {@link ShadowServiceManager#setServiceAvailability},
   * {@code null} will be returned.
   */
  @Implementation
  @Nullable
  protected Object getSystemService(String name) {
    if (removedSystemServices.contains(name)) {
      return null;
    }
    if (!systemServices.containsKey(name)) {
      return reflector(_ContextImpl_.class, realContextImpl).getSystemService(name);
    }
    return systemServices.get(name);
  }

  public void setSystemService(String key, Object service) {
    systemServices.put(key, service);
  }

  /**
   * Makes {@link #getSystemService(String)} return {@code null} for the given system service name,
   * mimicking a device that doesn't have that system service.
   */
  public void removeSystemService(String name) {
    removedSystemServices.add(name);
  }

  @Implementation
  protected void startIntentSender(
      IntentSender intent,
      Intent fillInIntent,
      int flagsMask,
      int flagsValues,
      int extraFlags,
      Bundle options)
      throws IntentSender.SendIntentException {
    intent.sendIntent(realContextImpl, 0, fillInIntent, null, null, null);
  }

  @Implementation
  protected ClassLoader getClassLoader() {
    return this.getClass().getClassLoader();
  }

  @Implementation
  protected int checkCallingPermission(String permission) {
    return checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid());
  }

  @Implementation
  protected int checkCallingOrSelfPermission(String permission) {
    return checkCallingPermission(permission);
  }

  @Implementation
  protected ContentResolver getContentResolver() {
    if (contentResolver == null) {
      contentResolver =
          new ContentResolver(realContextImpl) {
            @Override
            protected IContentProvider acquireProvider(Context c, String name) {
              return null;
            }

            @Override
            public boolean releaseProvider(IContentProvider icp) {
              return false;
            }

            @Override
            protected IContentProvider acquireUnstableProvider(Context c, String name) {
              return null;
            }

            @Override
            public boolean releaseUnstableProvider(IContentProvider icp) {
              return false;
            }

            @Override
            public void unstableProviderDied(IContentProvider icp) {}
          };
    }
    return contentResolver;
  }

  @Implementation
  protected void sendBroadcast(Intent intent) {
    getShadowInstrumentation()
        .sendBroadcastWithPermission(
            intent, /*userHandle=*/ null, /*receiverPermission=*/ null, realContextImpl);
  }

  @Implementation
  protected void sendBroadcast(Intent intent, String receiverPermission) {
    getShadowInstrumentation()
        .sendBroadcastWithPermission(
            intent, /*userHandle=*/ null, receiverPermission, realContextImpl);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  @RequiresPermission(android.Manifest.permission.INTERACT_ACROSS_USERS)
  protected void sendBroadcastAsUser(@RequiresPermission Intent intent, UserHandle user) {
    getShadowInstrumentation()
        .sendBroadcastWithPermission(intent, user, /*receiverPermission=*/ null, realContextImpl);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  @RequiresPermission(android.Manifest.permission.INTERACT_ACROSS_USERS)
  protected void sendBroadcastAsUser(
      @RequiresPermission Intent intent, UserHandle user, @Nullable String receiverPermission) {
    getShadowInstrumentation()
        .sendBroadcastWithPermission(intent, user, receiverPermission, realContextImpl);
  }

  @Implementation
  protected void sendOrderedBroadcast(Intent intent, String receiverPermission) {
    getShadowInstrumentation()
        .sendOrderedBroadcastWithPermission(intent, receiverPermission, realContextImpl);
  }

  @Implementation
  protected void sendOrderedBroadcast(
      Intent intent,
      String receiverPermission,
      BroadcastReceiver resultReceiver,
      Handler scheduler,
      int initialCode,
      String initialData,
      Bundle initialExtras) {
    getShadowInstrumentation()
        .sendOrderedBroadcastAsUser(
            intent,
            /*userHandle=*/ null,
            receiverPermission,
            resultReceiver,
            scheduler,
            initialCode,
            initialData,
            initialExtras,
            realContextImpl);
  }

  /**
   * Allows the test to query for the broadcasts for specific users, for everything else behaves as
   * {@link #sendOrderedBroadcastAsUser}.
   */
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected void sendOrderedBroadcastAsUser(
      Intent intent,
      UserHandle userHandle,
      String receiverPermission,
      BroadcastReceiver resultReceiver,
      Handler scheduler,
      int initialCode,
      String initialData,
      Bundle initialExtras) {
    getShadowInstrumentation()
        .sendOrderedBroadcastAsUser(
            intent,
            userHandle,
            receiverPermission,
            resultReceiver,
            scheduler,
            initialCode,
            initialData,
            initialExtras,
            realContextImpl);
  }

  /** Behaves as {@link #sendOrderedBroadcastAsUser}. Currently ignores appOp and options. */
  @Implementation(minSdk = M)
  protected void sendOrderedBroadcastAsUser(
      Intent intent,
      UserHandle userHandle,
      String receiverPermission,
      int appOp,
      Bundle options,
      BroadcastReceiver resultReceiver,
      Handler scheduler,
      int initialCode,
      String initialData,
      Bundle initialExtras) {
    sendOrderedBroadcastAsUser(
        intent,
        userHandle,
        receiverPermission,
        resultReceiver,
        scheduler,
        initialCode,
        initialData,
        initialExtras);
  }

  @Implementation
  protected void sendStickyBroadcast(Intent intent) {
    getShadowInstrumentation().sendStickyBroadcast(intent, realContextImpl);
  }

  @Implementation
  protected int checkPermission(String permission, int pid, int uid) {
    return getShadowInstrumentation().checkPermission(permission, pid, uid);
  }

  @Implementation
  protected Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    return getShadowInstrumentation().registerReceiver(receiver, filter, realContextImpl);
  }

  @Implementation
  protected Intent registerReceiver(
      BroadcastReceiver receiver,
      IntentFilter filter,
      String broadcastPermission,
      Handler scheduler) {
    return getShadowInstrumentation()
        .registerReceiver(receiver, filter, broadcastPermission, scheduler, realContextImpl);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected Intent registerReceiverAsUser(
      BroadcastReceiver receiver,
      UserHandle user,
      IntentFilter filter,
      String broadcastPermission,
      Handler scheduler) {
    return getShadowInstrumentation()
        .registerReceiverWithContext(
            receiver, filter, broadcastPermission, scheduler, realContextImpl);
  }

  @Implementation
  protected void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
    getShadowInstrumentation().unregisterReceiver(broadcastReceiver);
  }

  @Implementation
  protected ComponentName startService(Intent service) {
    validateServiceIntent(service);
    return getShadowInstrumentation().startService(service);
  }

  @Implementation(minSdk = O)
  protected ComponentName startForegroundService(Intent service) {
    return startService(service);
  }

  @Implementation
  protected boolean stopService(Intent name) {
    validateServiceIntent(name);
    return getShadowInstrumentation().stopService(name);
  }

  @Implementation
  protected boolean bindService(Intent intent, final ServiceConnection serviceConnection, int i) {
    validateServiceIntent(intent);
    return getShadowInstrumentation().bindService(intent, serviceConnection, i);
  }

  /** Binds to a service but ignores the given UserHandle. */
  @Implementation(minSdk = LOLLIPOP)
  protected boolean bindServiceAsUser(
      Intent intent, final ServiceConnection serviceConnection, int i, UserHandle userHandle) {
    return bindService(intent, serviceConnection, i);
  }

  @Implementation
  protected void unbindService(final ServiceConnection serviceConnection) {
    getShadowInstrumentation().unbindService(serviceConnection);
  }

  // This is a private method in ContextImpl so we copy the relevant portions of it here.
  private void validateServiceIntent(Intent service) {
    if (service.getComponent() == null
        && service.getPackage() == null
        && realContextImpl.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
      throw new IllegalArgumentException("Service Intent must be explicit: " + service);
    }
  }

  /**
   * Behaves as {@link android.app.ContextImpl#startActivity(Intent, Bundle)}. The user parameter is
   * ignored.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
    // TODO: Remove this once {@link com.android.server.wmActivityTaskManagerService} is
    // properly shadowed.
    reflector(_ContextImpl_.class, realContextImpl).startActivity(intent, options);
  }

  /* Set the user id returned by {@link #getUserId()}. */
  public void setUserId(int userId) {
    this.userId = userId;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected int getUserId() {
    if (userId != null) {
      return userId;
    } else {
      return directlyOn(realContextImpl, ShadowContextImpl.CLASS_NAME, "getUserId");
    }
  }

  @Implementation(maxSdk = JELLY_BEAN_MR2)
  protected File getExternalFilesDir(String type) {
    return Environment.getExternalStoragePublicDirectory(type);
  }

  @Implementation(minSdk = KITKAT)
  protected File[] getExternalFilesDirs(String type) {
    return new File[] {Environment.getExternalStoragePublicDirectory(type)};
  }

  @Resetter
  public static void reset() {
    String prefsCacheFieldName =
        RuntimeEnvironment.getApiLevel() >= N ? "sSharedPrefsCache" : "sSharedPrefs";
    Object prefsDefaultValue = RuntimeEnvironment.getApiLevel() >= KITKAT ? null : new HashMap<>();
    Class<?> contextImplClass =
        ReflectionHelpers.loadClass(
            ShadowContextImpl.class.getClassLoader(), "android.app.ContextImpl");
    ReflectionHelpers.setStaticField(contextImplClass, prefsCacheFieldName, prefsDefaultValue);

    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.LOLLIPOP_MR1) {
      HashMap<String, Object> fetchers =
          ReflectionHelpers.getStaticField(contextImplClass, "SYSTEM_SERVICE_MAP");
      Class staticServiceFetcherClass =
          ReflectionHelpers.loadClass(
              ShadowContextImpl.class.getClassLoader(),
              "android.app.ContextImpl$StaticServiceFetcher");

      for (Object o : fetchers.values()) {
        if (staticServiceFetcherClass.isInstance(o)) {
          ReflectionHelpers.setField(staticServiceFetcherClass, o, "mCachedInstance", null);
        }
      }

      if (RuntimeEnvironment.getApiLevel() >= KITKAT) {

        Object windowServiceFetcher = fetchers.get(Context.WINDOW_SERVICE);
        ReflectionHelpers.setField(
            windowServiceFetcher.getClass(), windowServiceFetcher, "mDefaultDisplay", null);
      }
    }
  }

  private ShadowInstrumentation getShadowInstrumentation() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return Shadow.extract(activityThread.getInstrumentation());
  }

  @Implementation
  public File getDatabasePath(String name) {
    // Windows is an abomination.
    if (File.separatorChar == '\\' && Paths.get(name).isAbsolute()) {
      String dirPath = name.substring(0, name.lastIndexOf(File.separatorChar));
      File dir = new File(dirPath);
      name = name.substring(name.lastIndexOf(File.separatorChar));
      File f = new File(dir, name);
      if (!dir.isDirectory() && dir.mkdir()) {
        FileUtils.setPermissions(dir.getPath(), 505, -1, -1);
      }
      return f;
    } else {
      return reflector(_ContextImpl_.class, realContextImpl).getDatabasePath(name);
    }
  }

  @Implementation
  protected SharedPreferences getSharedPreferences(String name, int mode) {
    // Windows does not allow colons in file names, which may be used in shared preference
    // names. URL-encode any colons in Windows.
    if (!Strings.isNullOrEmpty(name) && File.separatorChar == '\\') {
      name = name.replace(":", "%3A");
    }
    return reflector(_ContextImpl_.class, realContextImpl).getSharedPreferences(name, mode);
  }

  /** Reflector interface for {@link android.app.ContextImpl}'s internals. */
  @ForType(className = CLASS_NAME)
  public interface _ContextImpl_ {
    @Static
    Context createSystemContext(ActivityThread activityThread);

    @Static
    Context createAppContext(ActivityThread activityThread, LoadedApk loadedApk);

    @Static
    Context createActivityContext(
        ActivityThread mainThread,
        LoadedApk packageInfo,
        ActivityInfo activityInfo,
        IBinder activityToken,
        int displayId,
        Configuration overrideConfiguration);

    IBinder getActivityToken();

    void setOuterContext(Context context);

    @Direct
    Object getSystemService(String name);

    @Direct
    void startActivity(Intent intent, Bundle options);

    @Direct
    File getDatabasePath(String name);

    @Direct
    SharedPreferences getSharedPreferences(String name, int mode);

    @Accessor("mClassLoader")
    void setClassLoader(ClassLoader classLoader);
  }
}
