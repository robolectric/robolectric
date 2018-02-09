package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.newInstanceOf;

import android.accounts.IAccountManager;
import android.app.IWallpaperManager;
import android.app.admin.IDevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.IRestrictionsManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.hardware.SystemSensorManager;
import android.hardware.fingerprint.IFingerprintService;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IUserManager;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;
import android.view.accessibility.AccessibilityManager;
import android.view.autofill.IAutoFillManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(className = ShadowContextImpl.CLASS_NAME)
public class ShadowContextImpl {

  public static final String CLASS_NAME = "android.app.ContextImpl";
  private ContentResolver contentResolver;

  @RealObject
  private Context realObject;

  private Map<String, Object> systemServices = new HashMap<String, Object>();

  @Implementation
  public Object getSystemService(String name) {
    Object service = systemServices.get(name);
    if (service == null) {
      return directlyOn(realObject, ShadowContextImpl.CLASS_NAME, "getSystemService", ClassParameter.from(String.class, name));
    }
    return service;
  }

  public void setSystemService(String key, Object service) {
    systemServices.put(key, service);
  }

  @Implementation
  public void startIntentSender(IntentSender intent, Intent fillInIntent,
                    int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
    intent.sendIntent(realObject, 0, fillInIntent, null, null, null);
  }

  @Implementation
  public ComponentName startService(Intent service) {
    return ShadowApplication.getInstance().startService(service);
  }

  @Implementation(minSdk = O)
  public ComponentName startForegroundService(Intent service) {
    return ShadowApplication.getInstance().startService(service);
  }

  @Implementation
  public void sendBroadcast(Intent intent) {
    ShadowApplication.getInstance().sendBroadcast(intent);
  }

  @Implementation
  public ClassLoader getClassLoader() {
    return this.getClass().getClassLoader();
  }

  @Implementation
  public boolean bindService(Intent intent, final ServiceConnection serviceConnection, int i) {
    return ShadowApplication.getInstance().bindService(intent, serviceConnection, i);
  }

  @Implementation
  public void unbindService(final ServiceConnection serviceConnection) {
    ShadowApplication.getInstance().unbindService(serviceConnection);
  }

  @Implementation
  public int checkCallingPermission(String permission) {
    return checkPermission(permission, -1, -1);
  }

  @Implementation
  public int checkCallingOrSelfPermission(String permission) {
    return checkPermission(permission, -1, -1);
  }

  @Implementation
  public ContentResolver getContentResolver() {
    if (contentResolver == null) {
      contentResolver = new ContentResolver(realObject) {
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
        public void unstableProviderDied(IContentProvider icp) {

        }
      };
    }
    return contentResolver;
  }

  @Implementation
  public void sendBroadcast(Intent intent, String receiverPermission) {
    ShadowApplication.getInstance().sendBroadcast(intent, receiverPermission);
  }

  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
    ShadowApplication.getInstance().sendOrderedBroadcast(intent, receiverPermission);
  }

  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver,
                                   Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
    ShadowApplication.getInstance().sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode,
        initialData, initialExtras);
  }

  @Implementation
  public void sendStickyBroadcast(Intent intent) {
    ShadowApplication.getInstance().sendStickyBroadcast(intent);
  }

  @Implementation
  public int checkPermission(String permission, int pid, int uid) {
    return ShadowApplication.getInstance().checkPermission(permission, pid, uid);
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    return ShadowApplication.getInstance().registerReceiverWithContext(receiver, filter, null, null, realObject);
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
    return ShadowApplication.getInstance().registerReceiverWithContext(receiver, filter, broadcastPermission, scheduler, realObject);
  }

  @Implementation
  public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user,
      IntentFilter filter, String broadcastPermission, Handler scheduler) {
    return ShadowApplication.getInstance().registerReceiverWithContext(receiver, filter, broadcastPermission, scheduler, realObject);
  }

  @Implementation
  public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
    ShadowApplication.getInstance().unregisterReceiver(broadcastReceiver);
  }

  @Implementation
  public boolean stopService(Intent name) {
    return ShadowApplication.getInstance().stopService(name);
  }

  @Implementation
  public int getUserId() {
    return 0;
  }

  @Implementation
  public File getExternalCacheDir() {
    return Environment.getExternalStorageDirectory();
  }

  @Implementation(maxSdk = JELLY_BEAN_MR2)
  public File getExternalFilesDir(String type) {
    return Environment.getExternalStoragePublicDirectory(type);
  }

  @Implementation(minSdk = KITKAT)
  public File[] getExternalFilesDirs(String type) {
    return new File[] { Environment.getExternalStoragePublicDirectory(type) };
  }

  @Resetter
  public static void reset() {
    String prefsCacheFieldName = RuntimeEnvironment.getApiLevel() >= N ? "sSharedPrefsCache" : "sSharedPrefs";
    Object prefsDefaultValue = RuntimeEnvironment.getApiLevel() >= KITKAT ? null : new HashMap<>();
    Class<?> contextImplClass = ReflectionHelpers.loadClass(ShadowContextImpl.class.getClassLoader(), "android.app.ContextImpl");
    ReflectionHelpers.setStaticField(contextImplClass, prefsCacheFieldName, prefsDefaultValue);
  }
}
