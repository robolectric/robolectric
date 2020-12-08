package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

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
import android.app.usage.IStorageStatsManager;
import android.app.usage.IUsageStatsManager;
import android.content.Context;
import android.content.IClipboard;
import android.content.IRestrictionsManager;
import android.content.integrity.IAppIntegrityManager;
import android.content.pm.ICrossProfileApps;
import android.content.pm.IShortcutService;
import android.content.rollback.IRollbackManager;
import android.hardware.biometrics.IAuthService;
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
import android.net.wifi.aware.IWifiAwareManager;
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

  private static final Map<String, BinderService> binderServices = new HashMap<>();
  private static final Set<String> unavailableServices = new HashSet<>();

  static {
    addBinderService(Context.CLIPBOARD_SERVICE, IClipboard.class);
    addBinderService(Context.WIFI_P2P_SERVICE, IWifiP2pManager.class);
    addBinderService(Context.ACCOUNT_SERVICE, IAccountManager.class);
    addBinderService(Context.USB_SERVICE, IUsbManager.class);
    addBinderService(Context.LOCATION_SERVICE, ILocationManager.class);
    addBinderService(Context.INPUT_METHOD_SERVICE, IInputMethodManager.class);
    addBinderService(Context.ALARM_SERVICE, IAlarmManager.class);
    addBinderService(Context.POWER_SERVICE, IPowerManager.class);
    addBinderService(BatteryStats.SERVICE_NAME, IBatteryStats.class);
    addBinderService(Context.DROPBOX_SERVICE, IDropBoxManagerService.class);
    addBinderService(Context.DEVICE_POLICY_SERVICE, IDevicePolicyManager.class);
    addBinderService(Context.CONNECTIVITY_SERVICE, IConnectivityManager.class);
    addBinderService(Context.WIFI_SERVICE, IWifiManager.class);
    addBinderService(Context.SEARCH_SERVICE, ISearchManager.class);
    addBinderService(Context.UI_MODE_SERVICE, IUiModeManager.class);
    addBinderService(Context.NETWORK_POLICY_SERVICE, INetworkPolicyManager.class);
    addBinderService(Context.INPUT_SERVICE, IInputManager.class);
    addBinderService(Context.COUNTRY_DETECTOR, ICountryDetector.class);
    addBinderService(Context.NSD_SERVICE, INsdManager.class);
    addBinderService(Context.AUDIO_SERVICE, IAudioService.class);
    addBinderService(Context.APPWIDGET_SERVICE, IAppWidgetService.class);
    addBinderService(Context.NOTIFICATION_SERVICE, INotificationManager.class);
    addBinderService(Context.WALLPAPER_SERVICE, IWallpaperManager.class);

    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR1) {
      addBinderService(Context.USER_SERVICE, IUserManager.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= JELLY_BEAN_MR2) {
      addBinderService(Context.APP_OPS_SERVICE, IAppOpsService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= KITKAT) {
      addBinderService("batteryproperties", IBatteryPropertiesRegistrar.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      addBinderService(Context.RESTRICTIONS_SERVICE, IRestrictionsManager.class);
      addBinderService(Context.TRUST_SERVICE, ITrustManager.class);
      addBinderService(Context.JOB_SCHEDULER_SERVICE, IJobScheduler.class);
      addBinderService(Context.NETWORK_SCORE_SERVICE, INetworkScoreService.class);
      addBinderService(Context.USAGE_STATS_SERVICE, IUsageStatsManager.class);
      addBinderService(Context.MEDIA_ROUTER_SERVICE, IMediaRouterService.class);
      addBinderService(Context.MEDIA_SESSION_SERVICE, ISessionManager.class, true);
    }
    if (RuntimeEnvironment.getApiLevel() >= M) {
      addBinderService(Context.FINGERPRINT_SERVICE, IFingerprintService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= N) {
      addBinderService(Context.CONTEXTHUB_SERVICE, IContextHubService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
      addBinderService(Context.SHORTCUT_SERVICE, IShortcutService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      addBinderService("mount", IStorageManager.class);
      addBinderService(Context.WIFI_AWARE_SERVICE, IWifiAwareManager.class);
      addBinderService(Context.STORAGE_STATS_SERVICE, IStorageStatsManager.class);
    } else {
      addBinderService("mount", "android.os.storage.IMountService");
    }
    if (RuntimeEnvironment.getApiLevel() >= P) {
      addBinderService(Context.SLICE_SERVICE, ISliceManager.class);
      addBinderService(Context.CROSS_PROFILE_APPS_SERVICE, ICrossProfileApps.class);
      addBinderService(Context.WIFI_RTT_RANGING_SERVICE, IWifiRttManager.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      addBinderService(Context.BIOMETRIC_SERVICE, IBiometricService.class);
      addBinderService(Context.ROLE_SERVICE, IRoleManager.class);
      addBinderService(Context.ROLLBACK_SERVICE, IRollbackManager.class);
      addBinderService(Context.THERMAL_SERVICE, IThermalService.class);
      addBinderService(Context.BUGREPORT_SERVICE, IDumpstate.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= R) {
      addBinderService(Context.APP_INTEGRITY_SERVICE, IAppIntegrityManager.class);
      addBinderService(Context.AUTH_SERVICE, IAuthService.class);
    }
  }

  /**
   * A data class that holds descriptor information about binder services. It also holds the cached
   * binder object if it is requested by {@link #getService(String)}.
   */
  private static class BinderService {

    private final Class<? extends IInterface> clazz;
    private final String className;
    private final boolean useDeepBinder;
    private Binder cachedBinder;

    BinderService(Class<? extends IInterface> clazz, String className, boolean useDeepBinder) {
      this.clazz = clazz;
      this.className = className;
      this.useDeepBinder = useDeepBinder;
    }

    IBinder getBinder() {
      if (cachedBinder == null) {
        cachedBinder = new Binder();
        cachedBinder.attachInterface(
            useDeepBinder
                ? ReflectionHelpers.createDeepProxy(clazz)
                : ReflectionHelpers.createNullProxy(clazz),
            className);
      }
      return cachedBinder;
    }
  }

  protected static void addBinderService(String name, Class<? extends IInterface> clazz) {
    addBinderService(name, clazz, clazz.getCanonicalName(), false);
  }

  protected static void addBinderService(
      String name, Class<? extends IInterface> clazz, boolean useDeepBinder) {
    addBinderService(name, clazz, clazz.getCanonicalName(), useDeepBinder);
  }

  protected static void addBinderService(String name, String className) {
    Class<? extends IInterface> clazz;
    try {
      clazz = Class.forName(className).asSubclass(IInterface.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    addBinderService(name, clazz, className, false);
  }

  protected static void addBinderService(
      String name, Class<? extends IInterface> clazz, String className, boolean useDeepBinder) {
    binderServices.put(name, new BinderService(clazz, className, useDeepBinder));
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
    BinderService binderService = binderServices.get(name);
    if (binderService == null) {
      return null;
    }
    return binderService.getBinder();
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
