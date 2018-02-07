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
  private static final Map<String, String> SYSTEM_SERVICE_MAP = new HashMap<>();
  private ContentResolver contentResolver;

  @RealObject
  private Context realObject;

  static {
    // note that these are different!
    // They specify concrete classes within Robolectric for interfaces or abstract classes defined by Android
    SYSTEM_SERVICE_MAP.put(Context.WINDOW_SERVICE, "android.view.WindowManagerImpl");
    SYSTEM_SERVICE_MAP.put(Context.CLIPBOARD_SERVICE, "android.content.ClipboardManager");
    SYSTEM_SERVICE_MAP.put(Context.SENSOR_SERVICE, "android.hardware.SystemSensorManager");
    SYSTEM_SERVICE_MAP.put(Context.VIBRATOR_SERVICE, "org.robolectric.fakes.RoboVibrator");

    // the rest are as mapped in docs...
    SYSTEM_SERVICE_MAP.put(Context.LAYOUT_INFLATER_SERVICE, "android.view.LayoutInflater");
    SYSTEM_SERVICE_MAP.put(Context.ACTIVITY_SERVICE, "android.app.ActivityManager");
    SYSTEM_SERVICE_MAP.put(Context.POWER_SERVICE, "android.os.PowerManager");
    SYSTEM_SERVICE_MAP.put(Context.ALARM_SERVICE, "android.app.AlarmManager");
    SYSTEM_SERVICE_MAP.put(Context.NOTIFICATION_SERVICE, "android.app.NotificationManager");
    SYSTEM_SERVICE_MAP.put(Context.KEYGUARD_SERVICE, "android.app.KeyguardManager");
    SYSTEM_SERVICE_MAP.put(Context.LOCATION_SERVICE, "android.location.LocationManager");
    SYSTEM_SERVICE_MAP.put(Context.SEARCH_SERVICE, "android.app.SearchManager");
    SYSTEM_SERVICE_MAP.put(Context.STORAGE_SERVICE, "android.os.storage.StorageManager");
    SYSTEM_SERVICE_MAP.put(Context.CONNECTIVITY_SERVICE, "android.net.ConnectivityManager");
    SYSTEM_SERVICE_MAP.put(Context.WIFI_SERVICE, "android.net.wifi.WifiManager");
    SYSTEM_SERVICE_MAP.put(Context.AUDIO_SERVICE, "android.media.AudioManager");
    SYSTEM_SERVICE_MAP.put(Context.TELEPHONY_SERVICE, "android.telephony.TelephonyManager");
    SYSTEM_SERVICE_MAP.put(Context.INPUT_METHOD_SERVICE, "android.view.inputmethod.InputMethodManager");
    SYSTEM_SERVICE_MAP.put(Context.UI_MODE_SERVICE, "android.app.UiModeManager");
    SYSTEM_SERVICE_MAP.put(Context.DOWNLOAD_SERVICE, "android.app.DownloadManager");
    SYSTEM_SERVICE_MAP.put(Context.TEXT_SERVICES_MANAGER_SERVICE, "android.view.textservice.TextServicesManager");
    SYSTEM_SERVICE_MAP.put(Context.DEVICE_POLICY_SERVICE, "android.app.admin.DevicePolicyManager");
    SYSTEM_SERVICE_MAP.put(Context.DROPBOX_SERVICE, "android.os.DropBoxManager");
    SYSTEM_SERVICE_MAP.put(Context.MEDIA_ROUTER_SERVICE, "android.media.MediaRouter");
    SYSTEM_SERVICE_MAP.put(Context.ACCESSIBILITY_SERVICE, "android.view.accessibility.AccessibilityManager");
    SYSTEM_SERVICE_MAP.put(Context.ACCOUNT_SERVICE, "android.accounts.AccountManager");
    SYSTEM_SERVICE_MAP.put(Context.NFC_SERVICE, "android.nfc.NfcManager");
    SYSTEM_SERVICE_MAP.put(Context.WALLPAPER_SERVICE, "android.app.WallpaperManager");
    SYSTEM_SERVICE_MAP.put(Context.WIFI_P2P_SERVICE, "android.net.wifi.p2p.WifiP2pManager");
    SYSTEM_SERVICE_MAP.put(Context.USB_SERVICE, "android.hardware.usb.UsbManager");
    SYSTEM_SERVICE_MAP.put(Context.AUTOFILL_MANAGER_SERVICE, "android.view.autofill.AutofillManager");
    SYSTEM_SERVICE_MAP.put(Context.TEXT_CLASSIFICATION_SERVICE, "android.view.textclassifier.TextClassificationManager");

    if (getApiLevel() >= JELLY_BEAN_MR1) {
      SYSTEM_SERVICE_MAP.put(Context.DISPLAY_SERVICE, "android.hardware.display.DisplayManager");
      SYSTEM_SERVICE_MAP.put(Context.USER_SERVICE, "android.os.UserManager");
    }
    if (getApiLevel() >= JELLY_BEAN_MR2) {
      SYSTEM_SERVICE_MAP.put(Context.BLUETOOTH_SERVICE, "android.bluetooth.BluetoothManager");
    }
    if (getApiLevel() >= KITKAT) {
      SYSTEM_SERVICE_MAP.put(Context.PRINT_SERVICE, "android.print.PrintManager");
      SYSTEM_SERVICE_MAP.put(Context.APP_OPS_SERVICE, "android.app.AppOpsManager");
      SYSTEM_SERVICE_MAP.put(Context.CAPTIONING_SERVICE, "android.view.accessibility.CaptioningManager");
    }
    if (getApiLevel() >= LOLLIPOP) {
      SYSTEM_SERVICE_MAP.put(Context.JOB_SCHEDULER_SERVICE, "android.app.JobSchedulerImpl");
      SYSTEM_SERVICE_MAP.put(Context.NETWORK_SCORE_SERVICE, "android.net.NetworkScoreManager");
      SYSTEM_SERVICE_MAP.put(Context.TELECOM_SERVICE, "android.telecom.TelecomManager");
      SYSTEM_SERVICE_MAP.put(Context.MEDIA_SESSION_SERVICE, "android.media.session.MediaSessionManager");
      SYSTEM_SERVICE_MAP.put(Context.BATTERY_SERVICE, "android.os.BatteryManager");
      SYSTEM_SERVICE_MAP.put(Context.RESTRICTIONS_SERVICE, "android.content.RestrictionsManager");
    }
    if (getApiLevel() >= LOLLIPOP_MR1) {
      SYSTEM_SERVICE_MAP.put(Context.TELEPHONY_SUBSCRIPTION_SERVICE, "android.telephony.SubscriptionManager");
    }
    if (getApiLevel() >= M) {
      SYSTEM_SERVICE_MAP.put(Context.FINGERPRINT_SERVICE, "android.hardware.fingerprint.FingerprintManager");
    }
    if (getApiLevel() >= N_MR1) {
      SYSTEM_SERVICE_MAP.put(Context.SHORTCUT_SERVICE, "android.content.pm.ShortcutManager");
    }

    if (getApiLevel() >= M) {
      SYSTEM_SERVICE_MAP.put(Context.LAYOUT_INFLATER_SERVICE, "com.android.internal.policy.PhoneLayoutInflater");
    } else {
      SYSTEM_SERVICE_MAP.put(Context.LAYOUT_INFLATER_SERVICE, "com.android.internal.policy.impl.PhoneLayoutInflater");
    }
  }

  private Map<String, Object> systemServices = new HashMap<String, Object>();

  @Implementation
  public Object getSystemService(String name) {
    Object service = systemServices.get(name);
    if (service == null) {
      String serviceClassName = SYSTEM_SERVICE_MAP.get(name);
      if (serviceClassName == null) {
        System.err.println("WARNING: unknown service " + name);
        return null;
      }

      try {
        Class<?> clazz = Class.forName(serviceClassName);

        if (serviceClassName.equals("android.content.RestrictionsManager")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application),
              ClassParameter.from(IRestrictionsManager.class, null));
        } else if (serviceClassName.equals("android.app.admin.DevicePolicyManager")) {
          if (getApiLevel() >= N) {
            service = ReflectionHelpers.callConstructor(clazz,
                ClassParameter.from(Context.class, RuntimeEnvironment.application),
                ClassParameter.from(IDevicePolicyManager.class, null),
                ClassParameter.from(boolean.class, false));
          } else {
            service = ReflectionHelpers.callConstructor(clazz,
                ClassParameter.from(Context.class, RuntimeEnvironment.application),
                ClassParameter.from(Handler.class, null));
          }
        } else if (serviceClassName.equals("android.app.SearchManager")
            || serviceClassName.equals("android.app.ActivityManager")) {

          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application),
              ClassParameter.from(Handler.class, null));
        } else if (serviceClassName.equals("android.app.WallpaperManager")) {
          if (getApiLevel() <= O_MR1) {
            service = ReflectionHelpers.callConstructor(clazz,
                ClassParameter.from(Context.class, RuntimeEnvironment.application),
                ClassParameter.from(Handler.class, null));
          }
        } else if (serviceClassName.equals("android.os.storage.StorageManager")) {
          service = ReflectionHelpers.callConstructor(clazz);
        } else if (serviceClassName.equals("android.nfc.NfcManager") || serviceClassName.equals("android.telecom.TelecomManager")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application));
        } else if (serviceClassName.equals("android.hardware.display.DisplayManager") || serviceClassName.equals("android.telephony.SubscriptionManager")) {
          service = ReflectionHelpers.callConstructor(clazz, ClassParameter.from(Context.class, RuntimeEnvironment.application));
        } else if (serviceClassName.equals("android.view.accessibility.AccessibilityManager")) {
          service = AccessibilityManager.getInstance(realObject);
        } else if (getApiLevel() >= JELLY_BEAN_MR1 && serviceClassName.equals("android.view.WindowManagerImpl")) {
          Class<?> windowMgrImplClass = Class.forName("android.view.WindowManagerImpl");
          if (getApiLevel() >= N) {
            service = ReflectionHelpers.callConstructor(windowMgrImplClass,
                ClassParameter.from(Context.class, realObject));
          } else {
            Display display = ShadowDisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY);
            service = ReflectionHelpers.callConstructor(windowMgrImplClass,
                ClassParameter.from(Display.class, display));
          }
        } else if (serviceClassName.equals("android.accounts.AccountManager")) {
          service = ReflectionHelpers.callConstructor(Class.forName("android.accounts.AccountManager"),
                ClassParameter.from(Context.class, RuntimeEnvironment.application),
                ClassParameter.from(IAccountManager.class , null));
        } else if (serviceClassName.equals("android.net.wifi.p2p.WifiP2pManager")) {
          service = new WifiP2pManager(ReflectionHelpers.createNullProxy(IWifiP2pManager.class));
        } else if (getApiLevel() >= KITKAT && serviceClassName.equals("android.print.PrintManager")) {
          service = ReflectionHelpers.callConstructor(Class.forName("android.print.PrintManager"),
            ClassParameter.from(Context.class, RuntimeEnvironment.application),
            ClassParameter.from(android.print.IPrintManager.class, null),
            ClassParameter.from(int.class, -1),
            ClassParameter.from(int.class, -1));
        } else if (serviceClassName.equals("android.hardware.SystemSensorManager")) {
          if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
            service = new SystemSensorManager(RuntimeEnvironment.application, Looper.getMainLooper());
          } else {
            service = ReflectionHelpers.callConstructor(
                Class.forName(serviceClassName),
                ClassParameter.from(Looper.class, Looper.getMainLooper()));
          }
        } else if (getApiLevel() >= O && serviceClassName.equals("android.app.KeyguardManager")) {
          service =
              ReflectionHelpers.callConstructor(
                  clazz, ClassParameter.from(Context.class, RuntimeEnvironment.application));
        } else if (getApiLevel() >= KITKAT && serviceClassName.equals("android.view.accessibility.CaptioningManager")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application));
        } else if (serviceClassName.equals("android.os.UserManager")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application),
              ClassParameter.from(IUserManager.class, null));
        } else if (getApiLevel() >= M && serviceClassName.equals("android.hardware.fingerprint.FingerprintManager")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application),
              ClassParameter.from(IFingerprintService.class, null));
        } else if (getApiLevel() >= O && serviceClassName.equals("android.view.autofill.AutofillManager")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application),
              ClassParameter.from(IAutoFillManager.class, null));
        } else if (getApiLevel() >= O && serviceClassName.equals("android.view.textclassifier.TextClassificationManager")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application));
        } else if (serviceClassName.equals("com.android.internal.policy.impl.PhoneLayoutInflater") || serviceClassName.equals("com.android.internal.policy.PhoneLayoutInflater")) {
          service = ReflectionHelpers.callConstructor(clazz,
              ClassParameter.from(Context.class, RuntimeEnvironment.application));
        } else {
          service = newInstanceOf(clazz);
        }
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }

      systemServices.put(name, service);
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
  public void startActivity(Intent intent) {
    ShadowApplication.getInstance().startActivity(intent);
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
  public void startActivity(Intent intent, Bundle options) {
    ShadowApplication.getInstance().startActivity(intent, options);
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
