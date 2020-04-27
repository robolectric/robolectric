package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.accounts.IAccountManager;
import android.app.IAlarmManager;
import android.app.INotificationManager;
import android.app.ISearchManager;
import android.app.IWallpaperManager;
import android.app.admin.IDevicePolicyManager;
import android.app.job.IJobScheduler;
import android.app.role.IRoleManager;
import android.app.slice.ISliceManager;
import android.app.trust.ITrustManager;
import android.app.usage.IUsageStatsManager;
import android.content.Context;
import android.content.IClipboard;
import android.content.IRestrictionsManager;
import android.content.pm.ICrossProfileApps;
import android.content.pm.IShortcutService;
import android.hardware.biometrics.IBiometricService;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.input.IInputManager;
import android.hardware.location.IContextHubService;
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
import android.net.wifi.rtt.IWifiRttManager;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IPowerManager;
import android.os.IThermalService;
import android.os.IUserManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IStorageManager;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.appwidget.IAppWidgetService;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.view.IInputMethodManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link ServiceManager}. */
@SuppressWarnings("NewApi")
@Implements(value = ServiceManager.class, isInAndroidSdk = false)
public class ShadowServiceManager {

  private static final Map<String, IBinder> SERVICES;

  private static final Set<String> unavailableServices = new HashSet<>();

  static {
    Map<String, IBinder> map = new HashMap<>();
    map.put(
        Context.CLIPBOARD_SERVICE, createBinder(IClipboard.class, "android.content.IClipboard"));
    map.put(
        Context.WIFI_P2P_SERVICE,
        createBinder(IWifiP2pManager.class, "android.net.wifi.p2p.IWifiP2pManager"));
    map.put(
        Context.ACCOUNT_SERVICE,
        createBinder(IAccountManager.class, "android.accounts.IAccountManager"));
    map.put(
        Context.USB_SERVICE, createBinder(IUsbManager.class, "android.hardware.usb.IUsbManager"));
    map.put(
        Context.LOCATION_SERVICE,
        createBinder(ILocationManager.class, "android.location.ILocationManager"));
    map.put(
        Context.INPUT_METHOD_SERVICE,
        createBinder(IInputMethodManager.class, "com.android.internal.view.IInputMethodManager"));
    map.put(Context.ALARM_SERVICE, createBinder(IAlarmManager.class, "android.app.IAlarmManager"));
    map.put(Context.POWER_SERVICE, createBinder(IPowerManager.class, "android.os.IPowerManager"));
    map.put(
        BatteryStats.SERVICE_NAME,
        createBinder(IBatteryStats.class, "com.android.internal.app.IBatteryStats"));
    map.put(
        Context.DROPBOX_SERVICE,
        createBinder(
            IDropBoxManagerService.class, "com.android.internal.os.IDropBoxManagerService"));
    map.put(
        Context.DEVICE_POLICY_SERVICE,
        createBinder(IDevicePolicyManager.class, "android.app.admin.IDevicePolicyManager"));
    map.put(
        Context.CONNECTIVITY_SERVICE,
        createBinder(IConnectivityManager.class, "android.net.IConnectivityManager"));
    map.put(
        Context.WIFI_SERVICE, createBinder(IWifiManager.class, "android.net.wifi.IWifiManager"));
    map.put(
        Context.SEARCH_SERVICE, createBinder(ISearchManager.class, "android.app.ISearchManager"));
    map.put(
        Context.UI_MODE_SERVICE, createBinder(ISearchManager.class, "android.app.IUiModeManager"));
    map.put(
        Context.NETWORK_POLICY_SERVICE,
        createBinder(ISearchManager.class, "android.net.INetworkPolicyManager"));
    map.put(Context.INPUT_SERVICE, createBinder(IInputManager.class, "android.net.IInputManager"));
    map.put(
        Context.COUNTRY_DETECTOR,
        createBinder(ICountryDetector.class, "android.location.ICountryDetector"));
    map.put(
        Context.NSD_SERVICE, createBinder(INsdManager.class, "android.net.nsd.INsdManagerandroi"));
    map.put(
        Context.AUDIO_SERVICE, createBinder(IAudioService.class, "android.media.IAudioService"));
    map.put(
        Context.APPWIDGET_SERVICE,
        createBinder(IAppWidgetService.class, "com.android.internal.appwidget.IAppWidgetService"));
    map.put(
        Context.NOTIFICATION_SERVICE,
        createBinder(INotificationManager.class, "android.app.INotificationManager"));
    map.put(
        Context.WALLPAPER_SERVICE,
        createBinder(IWallpaperManager.class, "android.app.IWallpaperManager"));

    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR1) {
      map.put(Context.USER_SERVICE, createBinder(IUserManager.class, "android.os.IUserManager"));
    }
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
      map.put(
          Context.APP_OPS_SERVICE,
          createBinder(IAppOpsService.class, "com.android.internal.app.IAppOpsService"));
    }
    if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
      map.put(
          "batteryproperties",
          createBinder(
              IBatteryPropertiesRegistrar.class, "android.os.IBatteryPropertiesRegistrar"));
    }
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      map.put(
          Context.RESTRICTIONS_SERVICE,
          createBinder(IRestrictionsManager.class, "android.content.IRestrictionsManager"));
      map.put(
          Context.TRUST_SERVICE,
          createBinder(ITrustManager.class, "android.app.trust.ITrustManager"));
      map.put(
          Context.JOB_SCHEDULER_SERVICE,
          createBinder(IJobScheduler.class, "android.app.job.IJobScheduler"));
      map.put(
          Context.NETWORK_SCORE_SERVICE,
          createBinder(INetworkScoreService.class, "android.net.INetworkScoreService"));
      map.put(
          Context.USAGE_STATS_SERVICE,
          createBinder(IUsageStatsManager.class, "android.app.usage.IUsageStatsManager"));
      map.put(
          Context.MEDIA_ROUTER_SERVICE,
          createBinder(IMediaRouterService.class, "android.media.IMediaRouterService"));
      map.put(
          Context.MEDIA_SESSION_SERVICE,
          createDeepBinder(ISessionManager.class, "android.media.session.ISessionManager"));
    }
    if (RuntimeEnvironment.getApiLevel() >= M) {
      map.put(
          Context.FINGERPRINT_SERVICE,
          createBinder(
              IFingerprintService.class, "android.hardware.fingerprint.IFingerprintService"));
    }
    if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
      map.put(
          Context.SHORTCUT_SERVICE,
          createBinder(IShortcutService.class, "android.content.pm.IShortcutService"));
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      map.put("mount", createBinder(IStorageManager.class, "android.os.storage.IStorageManager"));
    } else {
      map.put(
          "mount",
          createBinder("android.os.storage.IMountService", "android.os.storage.IMountService"));
    }
    if (RuntimeEnvironment.getApiLevel() >= P) {
      map.put(
          Context.SLICE_SERVICE,
          createBinder(ISliceManager.class, "android.app.slice.SliceManager"));
      map.put(
          Context.CROSS_PROFILE_APPS_SERVICE,
          createBinder(ICrossProfileApps.class, "android.content.pm.ICrossProfileApps"));
      map.put(
          Context.WIFI_RTT_RANGING_SERVICE,
          createBinder(IWifiRttManager.class, "android.net.wifi.IWifiRttManager"));
      map.put(
          Context.CONTEXTHUB_SERVICE,
          createBinder(IContextHubService.class, "android.hardware.location.IContextHubService"));
    }
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      map.put(
          Context.BIOMETRIC_SERVICE,
          createBinder(IBiometricService.class, "android.hardware.biometrics.IBiometricService"));
      map.put(
          Context.ROLE_SERVICE, createBinder(IRoleManager.class, "android.app.role.IRoleManager"));
      map.put(
          Context.THERMAL_SERVICE,
          createBinder(IThermalService.class, "android.os.IThermalService"));
    }

    SERVICES = Collections.unmodifiableMap(map);
  }

  /**
   * Returns the binder associated with the given system service. If the given service is set to
   * unavailable in {@link #setServiceAvailability}, {@code null} will be returned.
   */
  @Implementation
  protected static IBinder getService(String name) {
    if (unavailableServices.contains(name)) {
      return null;
    }
    return SERVICES.get(name);
  }

  private static Binder createBinder(String className, String descriptor) {
    Class<IInterface> clazz;
    try {
      clazz = (Class<IInterface>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createNullProxy(clazz), descriptor);
    return binder;
  }

  protected static Binder createBinder(Class<? extends IInterface> clazz, String descriptor) {
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
  protected static void addService(String name, IBinder service) {}

  @Implementation
  protected static IBinder checkService(String name) {
    return null;
  }

  @Implementation
  protected static String[] listServices() throws RemoteException {
    return null;
  }

  @Implementation
  protected static void initServiceCache(Map<String, IBinder> cache) {}

  /**
   * Sets the availability of the given system service. If the service is set as unavailable,
   * subsequent calls to {@link Context#getSystemService} for that service will return {@code null}.
   */
  public static void setServiceAvailability(String service, boolean available) {
    if (available) {
      unavailableServices.remove(service);
    } else {
      unavailableServices.add(service);
    }
  }

  @Resetter
  public static void reset() {
    unavailableServices.clear();
  }
}
