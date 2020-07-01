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
import android.app.IUiModeManager;
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
import android.content.rollback.IRollbackManager;
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
import android.net.INetworkPolicyManager;
import android.net.INetworkScoreService;
import android.net.nsd.INsdManager;
import android.net.wifi.IWifiManager;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.rtt.IWifiRttManager;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.IBatteryPropertiesRegistrar;
import android.os.IBinder;
import android.os.IDumpstate;
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
    map.put(Context.CLIPBOARD_SERVICE, createBinder(IClipboard.class));
    map.put(Context.WIFI_P2P_SERVICE, createBinder(IWifiP2pManager.class));
    map.put(Context.ACCOUNT_SERVICE, createBinder(IAccountManager.class));
    map.put(Context.USB_SERVICE, createBinder(IUsbManager.class));
    map.put(Context.LOCATION_SERVICE, createBinder(ILocationManager.class));
    map.put(Context.INPUT_METHOD_SERVICE, createBinder(IInputMethodManager.class));
    map.put(Context.ALARM_SERVICE, createBinder(IAlarmManager.class));
    map.put(Context.POWER_SERVICE, createBinder(IPowerManager.class));
    map.put(BatteryStats.SERVICE_NAME, createBinder(IBatteryStats.class));
    map.put(Context.DROPBOX_SERVICE, createBinder(IDropBoxManagerService.class));
    map.put(Context.DEVICE_POLICY_SERVICE, createBinder(IDevicePolicyManager.class));
    map.put(Context.CONNECTIVITY_SERVICE, createBinder(IConnectivityManager.class));
    map.put(Context.WIFI_SERVICE, createBinder(IWifiManager.class));
    map.put(Context.SEARCH_SERVICE, createBinder(ISearchManager.class));
    map.put(Context.UI_MODE_SERVICE, createBinder(IUiModeManager.class));
    map.put(Context.NETWORK_POLICY_SERVICE, createBinder(INetworkPolicyManager.class));
    map.put(Context.INPUT_SERVICE, createBinder(IInputManager.class));
    map.put(Context.COUNTRY_DETECTOR, createBinder(ICountryDetector.class));
    map.put(Context.NSD_SERVICE, createBinder(INsdManager.class));
    map.put(Context.AUDIO_SERVICE, createBinder(IAudioService.class));
    map.put(Context.APPWIDGET_SERVICE, createBinder(IAppWidgetService.class));
    map.put(Context.NOTIFICATION_SERVICE, createBinder(INotificationManager.class));
    map.put(Context.WALLPAPER_SERVICE, createBinder(IWallpaperManager.class));

    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR1) {
      map.put(Context.USER_SERVICE, createBinder(IUserManager.class));
    }
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
      map.put(Context.APP_OPS_SERVICE, createBinder(IAppOpsService.class));
    }
    if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
      map.put("batteryproperties", createBinder(IBatteryPropertiesRegistrar.class));
    }
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      map.put(Context.RESTRICTIONS_SERVICE, createBinder(IRestrictionsManager.class));
      map.put(Context.TRUST_SERVICE, createBinder(ITrustManager.class));
      map.put(Context.JOB_SCHEDULER_SERVICE, createBinder(IJobScheduler.class));
      map.put(Context.NETWORK_SCORE_SERVICE, createBinder(INetworkScoreService.class));
      map.put(Context.USAGE_STATS_SERVICE, createBinder(IUsageStatsManager.class));
      map.put(Context.MEDIA_ROUTER_SERVICE, createBinder(IMediaRouterService.class));
      map.put(Context.MEDIA_SESSION_SERVICE, createDeepBinder(ISessionManager.class));
    }
    if (RuntimeEnvironment.getApiLevel() >= M) {
      map.put(Context.FINGERPRINT_SERVICE, createBinder(IFingerprintService.class));
    }
    if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
      map.put(Context.SHORTCUT_SERVICE, createBinder(IShortcutService.class));
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      map.put("mount", createBinder(IStorageManager.class));
    } else {
      map.put("mount", createBinder("android.os.storage.IMountService"));
    }
    if (RuntimeEnvironment.getApiLevel() >= P) {
      map.put(Context.SLICE_SERVICE, createBinder(ISliceManager.class));
      map.put(Context.CROSS_PROFILE_APPS_SERVICE, createBinder(ICrossProfileApps.class));
      map.put(Context.WIFI_RTT_RANGING_SERVICE, createBinder(IWifiRttManager.class));
      map.put(Context.CONTEXTHUB_SERVICE, createBinder(IContextHubService.class));
    }
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      map.put(Context.BIOMETRIC_SERVICE, createBinder(IBiometricService.class));
      map.put(Context.ROLE_SERVICE, createBinder(IRoleManager.class));
      map.put(Context.ROLLBACK_SERVICE, createBinder(IRollbackManager.class));
      map.put(Context.THERMAL_SERVICE, createBinder(IThermalService.class));
      map.put(Context.BUGREPORT_SERVICE, createBinder(IDumpstate.class));
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

  private static Binder createBinder(String className) {
    Class<IInterface> clazz;
    try {
      clazz = (Class<IInterface>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createNullProxy(clazz), className);
    return binder;
  }

  protected static Binder createBinder(Class<? extends IInterface> clazz) {
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createNullProxy(clazz), clazz.getCanonicalName());
    return binder;
  }

  private static Binder createDeepBinder(Class<? extends IInterface> clazz) {
    Binder binder = new Binder();
    binder.attachInterface(ReflectionHelpers.createDeepProxy(clazz), clazz.getCanonicalName());
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
