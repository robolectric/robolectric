package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;

import android.accounts.IAccountManager;
import android.app.IAlarmManager;
import android.app.ISearchManager;
import android.app.admin.IDevicePolicyManager;
import android.app.job.IJobScheduler;
import android.app.trust.ITrustManager;
import android.app.usage.IUsageStatsManager;
import android.content.Context;
import android.content.IClipboard;
import android.content.IRestrictionsManager;
import android.content.pm.IShortcutService;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.input.IInputManager;
import android.hardware.usb.IUsbManager;
import android.location.ICountryDetector;
import android.location.ILocationManager;
import android.media.IAudioService;
import android.media.IMediaRouterService;
import android.media.session.ISessionManager;
import android.net.IConnectivityManager;
import android.net.INetworkScoreService;
import android.net.nsd.INsdManager;
import android.net.wifi.IWifiManager;
import android.net.wifi.p2p.IWifiP2pManager;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IPowerManager;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IStorageManager;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.view.IInputMethodManager;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = ServiceManager.class, isInAndroidSdk = false)
public class ShadowServiceManager {

  private static Map<String, IBinder> SERVICES =
      new HashMap<String, IBinder>() {
        {
          put(
              Context.CLIPBOARD_SERVICE,
              createBinder(IClipboard.class, "android.content.IClipboard"));
          put(
              Context.WIFI_P2P_SERVICE,
              createBinder(IWifiP2pManager.class, "android.net.wifi.p2p.IWifiP2pManager"));
          put(
              Context.ACCOUNT_SERVICE,
              createBinder(IAccountManager.class, "android.accounts.IAccountManager"));
          put(
              Context.USB_SERVICE,
              createBinder(IUsbManager.class, "android.hardware.usb.IUsbManager"));
          put(
              Context.LOCATION_SERVICE,
              createBinder(ILocationManager.class, "android.location.ILocationManager"));
          put(
              Context.INPUT_METHOD_SERVICE,
              createBinder(
                  IInputMethodManager.class, "com.android.internal.view.IInputMethodManager"));
          put(
              Context.ALARM_SERVICE,
              createBinder(IAlarmManager.class, "android.app.IAlarmManager"));
          put(Context.POWER_SERVICE, createBinder(IPowerManager.class, "android.os.IPowerManager"));
          put(
              BatteryStats.SERVICE_NAME,
              createBinder(IBatteryStats.class, "com.android.internal.app.IBatteryStats"));
          put(
              Context.DROPBOX_SERVICE,
              createBinder(
                  IDropBoxManagerService.class, "com.android.internal.os.IDropBoxManagerService"));
          put(
              Context.DEVICE_POLICY_SERVICE,
              createBinder(IDevicePolicyManager.class, "android.app.admin.IDevicePolicyManager"));
          put(
              Context.CONNECTIVITY_SERVICE,
              createBinder(IConnectivityManager.class, "android.net.IConnectivityManager"));
          put(
              Context.WIFI_SERVICE,
              createBinder(IWifiManager.class, "android.net.wifi.IWifiManager"));
          put(
              Context.SEARCH_SERVICE,
              createBinder(ISearchManager.class, "android.app.ISearchManager"));
          put(
              Context.UI_MODE_SERVICE,
              createBinder(ISearchManager.class, "android.app.IUiModeManager"));
          put(
              Context.NETWORK_POLICY_SERVICE,
              createBinder(ISearchManager.class, "android.net.INetworkPolicyManager"));
          put(
              Context.INPUT_SERVICE,
              createBinder(IInputManager.class, "android.net.IInputManager"));
          put(
              Context.COUNTRY_DETECTOR,
              createBinder(ICountryDetector.class, "android.location.ICountryDetector"));
          put(
              Context.NSD_SERVICE,
              createBinder(INsdManager.class, "android.net.nsd.INsdManagerandroi"));
          put(
              Context.AUDIO_SERVICE,
              createBinder(IAudioService.class, "android.media.IAudioService"));

          if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR1) {
            put(Context.USER_SERVICE, createBinder(IUserManager.class, "android.os.IUserManager"));
          }
          if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
            put(
                Context.APP_OPS_SERVICE,
                createBinder(IAppOpsService.class, "com.android.internal.app.IAppOpsService"));
          }
          if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
            put(
                "batteryproperties",
                createBinder(
                    IBatteryPropertiesRegistrar.class, "android.os.IBatteryPropertiesRegistrar"));
          }
          if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
            put(
                Context.RESTRICTIONS_SERVICE,
                createBinder(IRestrictionsManager.class, "android.content.IRestrictionsManager"));
            put(
                Context.TRUST_SERVICE,
                createBinder(ITrustManager.class, "android.app.trust.ITrustManager"));
            put(
                Context.JOB_SCHEDULER_SERVICE,
                createBinder(IJobScheduler.class, "android.app.job.IJobScheduler"));
            put(
                Context.NETWORK_SCORE_SERVICE,
                createBinder(INetworkScoreService.class, "android.net.INetworkScoreService"));
            put(
                Context.USAGE_STATS_SERVICE,
                createBinder(IUsageStatsManager.class, "android.app.usage.IUsageStatsManager"));
            put(
                Context.MEDIA_ROUTER_SERVICE,
                createBinder(IMediaRouterService.class, "android.media.IMediaRouterService"));
            put(
                Context.MEDIA_SESSION_SERVICE,
                createDeepBinder(ISessionManager.class, "android.media.session.ISessionManager"));
          }
          if (RuntimeEnvironment.getApiLevel() >= M) {
            put(
                Context.FINGERPRINT_SERVICE,
                createBinder(
                    IFingerprintService.class, "android.hardware.fingerprint.IFingerprintService"));
          }
          if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
            put(
                Context.SHORTCUT_SERVICE,
                createBinder(IShortcutService.class, "android.content.pm.IShortcutService"));
          }
          if (RuntimeEnvironment.getApiLevel() >= O) {
            put("mount", createBinder(IStorageManager.class, "android.os.storage.IStorageManager"));
          } else {
            put(
                "mount",
                createBinder(
                    "android.os.storage.IMountService", "android.os.storage.IMountService"));
          }
        }
      };

  @Implementation
  public static IBinder getService(String name) {
    return SERVICES.get(name);
  }

  private static Binder createBinder(String className, String descriptor) {
    Class<IInterface> clazz = null;
    try {
      clazz = (Class<IInterface>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createNullProxy(clazz), descriptor);
    return binder;
  }

  private static Binder createBinder(Class<? extends IInterface> clazz, String descriptor) {
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createNullProxy(clazz), descriptor);
    return binder;
  }

  private static Binder createDeepBinder(Class<? extends IInterface> clazz, String descriptor) {
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createDeepProxy(clazz), descriptor);
    return binder;
  }

  @Implementation
  public static void addService(String name, IBinder service) {
  }

  @Implementation
  public static IBinder checkService(String name) {
    return null;
  }

  @Implementation
  public static String[] listServices() throws RemoteException {
    return null;
  }

  @Implementation
  public static void initServiceCache(Map<String, IBinder> cache) {
  }
}
