package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.base.Preconditions.checkNotNull;

import android.accounts.IAccountManager;
import android.app.IAlarmManager;
import android.app.ILocaleManager;
import android.app.INotificationManager;
import android.app.ISearchManager;
import android.app.IUiModeManager;
import android.app.IWallpaperManager;
import android.app.admin.IDevicePolicyManager;
import android.app.ambientcontext.IAmbientContextManager;
import android.app.job.IJobScheduler;
import android.app.role.IRoleManager;
import android.app.slice.ISliceManager;
import android.app.timedetector.ITimeDetectorService;
import android.app.timezonedetector.ITimeZoneDetectorService;
import android.app.trust.ITrustManager;
import android.app.usage.IStorageStatsManager;
import android.app.usage.IUsageStatsManager;
import android.app.wearable.IWearableSensingManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothManager;
import android.companion.ICompanionDeviceManager;
import android.companion.virtual.IVirtualDeviceManager;
import android.content.Context;
import android.content.IClipboard;
import android.content.IRestrictionsManager;
import android.content.integrity.IAppIntegrityManager;
import android.content.pm.ICrossProfileApps;
import android.content.pm.ILauncherApps;
import android.content.pm.IShortcutService;
import android.content.rollback.IRollbackManager;
import android.hardware.ISensorPrivacyManager;
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
import android.net.IIpSecService;
import android.net.INetworkPolicyManager;
import android.net.INetworkScoreService;
import android.net.ITetheringConnector;
import android.net.IVpnManager;
import android.net.nsd.INsdManager;
import android.net.vcn.IVcnManagementService;
import android.net.wifi.IWifiManager;
import android.net.wifi.IWifiScanner;
import android.net.wifi.aware.IWifiAwareManager;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.rtt.IWifiRttManager;
import android.nfc.INfcAdapter;
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
import android.permission.ILegacyPermissionManager;
import android.permission.IPermissionManager;
import android.safetycenter.ISafetyCenterManager;
import android.security.IFileIntegrityService;
import android.speech.IRecognitionServiceManager;
import android.uwb.IUwbAdapter;
import android.view.IWindowManager;
import android.view.contentcapture.IContentCaptureManager;
import android.view.translation.ITranslationManager;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.ISoundTriggerService;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.internal.appwidget.IAppWidgetService;
import com.android.internal.compat.IPlatformCompat;
import com.android.internal.os.IDropBoxManagerService;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.view.IInputMethodManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link ServiceManager}. */
@SuppressWarnings("NewApi")
@Implements(value = ServiceManager.class, isInAndroidSdk = false)
public class ShadowServiceManager {

  // A mutable map that contains a list of binder services. It is mutable so entries can be added by
  // ShadowServiceManager subclasses. This is useful to support prerelease SDKs.
  protected static final Map<String, BinderService> binderServices = buildBinderServicesMap();

  @GuardedBy("ShadowServiceManager.class")
  private static final Set<String> unavailableServices = new HashSet<>();

  /** Represents the type of implementation to generate for the Binder interface */
  private enum BinderProxyType {
    /* use ReflectionHelpers.createNullProxy */
    NULL,
    /* use ReflectionHelpers.createDeepProxy */
    DEEP,
    /* use ReflectionHelpers.createDelegatingProxy */
    DELEGATING
  }

  /**
   * A data class that holds descriptor information about binder services. It also holds the cached
   * binder object if it is requested by {@link #getService(String)}.
   */
  private static final class BinderService {

    private final Class<? extends IInterface> clazz;
    private final String className;
    private final BinderProxyType proxyType;
    private Binder cachedBinder;
    private final Object delegate;

    BinderService(
        Class<? extends IInterface> clazz,
        String className,
        BinderProxyType proxyType,
        @Nullable Object delegate) {
      this.clazz = clazz;
      this.className = className;
      this.proxyType = proxyType;
      this.delegate = delegate;
      if (proxyType == BinderProxyType.DELEGATING) {
        checkNotNull(delegate);
      }
    }

    @GuardedBy("ShadowServiceManager.class")
    IBinder getBinder() {
      if (cachedBinder == null) {
        cachedBinder = new Binder();

        cachedBinder.attachInterface(createProxy(), className);
      }
      return cachedBinder;
    }

    private IInterface createProxy() {
      switch (proxyType) {
        case NULL:
          return ReflectionHelpers.createNullProxy(clazz);
        case DEEP:
          return ReflectionHelpers.createDeepProxy(clazz);
        case DELEGATING:
          return ReflectionHelpers.createDelegatingProxy(clazz, delegate);
      }
      throw new IllegalStateException("unrecognized proxy type " + proxyType);
    }
  }

  private static Map<String, BinderService> buildBinderServicesMap() {
    Map<String, BinderService> binderServices = new HashMap<>();
    addBinderService(binderServices, Context.CLIPBOARD_SERVICE, IClipboard.class);
    addBinderService(binderServices, Context.WIFI_P2P_SERVICE, IWifiP2pManager.class);
    addBinderService(binderServices, Context.ACCOUNT_SERVICE, IAccountManager.class);
    addBinderService(binderServices, Context.USB_SERVICE, IUsbManager.class);
    addBinderService(binderServices, Context.LOCATION_SERVICE, ILocationManager.class);
    addBinderService(binderServices, Context.INPUT_METHOD_SERVICE, IInputMethodManager.class);
    addBinderService(binderServices, Context.ALARM_SERVICE, IAlarmManager.class);
    addBinderService(binderServices, Context.POWER_SERVICE, IPowerManager.class);
    addBinderService(binderServices, BatteryStats.SERVICE_NAME, IBatteryStats.class);
    addBinderService(binderServices, Context.DROPBOX_SERVICE, IDropBoxManagerService.class);
    addBinderService(binderServices, Context.DEVICE_POLICY_SERVICE, IDevicePolicyManager.class);
    addBinderService(binderServices, Context.TELEPHONY_SERVICE, ITelephony.class);
    addBinderService(binderServices, Context.CONNECTIVITY_SERVICE, IConnectivityManager.class);
    addBinderService(binderServices, Context.WIFI_SERVICE, IWifiManager.class);
    addBinderService(binderServices, Context.SEARCH_SERVICE, ISearchManager.class);
    addBinderService(binderServices, Context.UI_MODE_SERVICE, IUiModeManager.class);
    addBinderService(binderServices, Context.NETWORK_POLICY_SERVICE, INetworkPolicyManager.class);
    addBinderService(binderServices, Context.INPUT_SERVICE, IInputManager.class);
    addBinderService(binderServices, Context.COUNTRY_DETECTOR, ICountryDetector.class);
    addBinderService(binderServices, Context.NSD_SERVICE, INsdManager.class);
    addBinderService(binderServices, Context.AUDIO_SERVICE, IAudioService.class);
    addBinderService(binderServices, Context.APPWIDGET_SERVICE, IAppWidgetService.class);
    addBinderService(binderServices, Context.NOTIFICATION_SERVICE, INotificationManager.class);
    addBinderService(binderServices, Context.WALLPAPER_SERVICE, IWallpaperManager.class);
    addBinderService(binderServices, Context.BLUETOOTH_SERVICE, IBluetooth.class);
    addBinderService(binderServices, Context.WINDOW_SERVICE, IWindowManager.class);
    addBinderService(binderServices, Context.NFC_SERVICE, INfcAdapter.class, BinderProxyType.DEEP);
    addBinderService(binderServices, Context.USER_SERVICE, IUserManager.class);
    addBinderService(
        binderServices,
        BluetoothAdapter.BLUETOOTH_MANAGER_SERVICE,
        IBluetoothManager.class,
        BinderProxyType.DELEGATING,
        IBluetoothManagerDelegates.createDelegate());

    addBinderService(binderServices, Context.APP_OPS_SERVICE, IAppOpsService.class);
    addBinderService(binderServices, "batteryproperties", IBatteryPropertiesRegistrar.class);

    addBinderService(binderServices, Context.RESTRICTIONS_SERVICE, IRestrictionsManager.class);
    addBinderService(binderServices, Context.TRUST_SERVICE, ITrustManager.class);
    addBinderService(binderServices, Context.JOB_SCHEDULER_SERVICE, IJobScheduler.class);
    addBinderService(binderServices, Context.NETWORK_SCORE_SERVICE, INetworkScoreService.class);
    addBinderService(binderServices, Context.USAGE_STATS_SERVICE, IUsageStatsManager.class);
    addBinderService(binderServices, Context.MEDIA_ROUTER_SERVICE, IMediaRouterService.class);
    addBinderService(
        binderServices, Context.MEDIA_SESSION_SERVICE, ISessionManager.class, BinderProxyType.DEEP);
    addBinderService(
        binderServices,
        Context.VOICE_INTERACTION_MANAGER_SERVICE,
        IVoiceInteractionManagerService.class,
        BinderProxyType.DEEP);
    addBinderService(
        binderServices,
        Context.LAUNCHER_APPS_SERVICE,
        ILauncherApps.class,
        BinderProxyType.DELEGATING,
        new LauncherAppsDelegate());

    if (RuntimeEnvironment.getApiLevel() >= M) {
      addBinderService(binderServices, Context.FINGERPRINT_SERVICE, IFingerprintService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= N) {
      addBinderService(binderServices, Context.CONTEXTHUB_SERVICE, IContextHubService.class);
      addBinderService(binderServices, Context.SOUND_TRIGGER_SERVICE, ISoundTriggerService.class);
      addBinderService(
          binderServices,
          Context.WIFI_SCANNING_SERVICE,
          IWifiScanner.class,
          BinderProxyType.DELEGATING,
          new WifiScannerDelegate());
    }
    if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
      addBinderService(binderServices, Context.SHORTCUT_SERVICE, IShortcutService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      addBinderService(binderServices, "mount", IStorageManager.class);
      addBinderService(binderServices, Context.WIFI_AWARE_SERVICE, IWifiAwareManager.class);
      addBinderService(binderServices, Context.STORAGE_STATS_SERVICE, IStorageStatsManager.class);
      addBinderService(
          binderServices, Context.COMPANION_DEVICE_SERVICE, ICompanionDeviceManager.class);
    } else {
      addBinderService(binderServices, "mount", "android.os.storage.IMountService");
    }
    if (RuntimeEnvironment.getApiLevel() >= P) {
      addBinderService(binderServices, Context.SLICE_SERVICE, ISliceManager.class);
      addBinderService(binderServices, Context.CROSS_PROFILE_APPS_SERVICE, ICrossProfileApps.class);
      addBinderService(binderServices, Context.WIFI_RTT_RANGING_SERVICE, IWifiRttManager.class);
      addBinderService(binderServices, Context.IPSEC_SERVICE, IIpSecService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= Q) {
      addBinderService(binderServices, Context.BIOMETRIC_SERVICE, IBiometricService.class);
      addBinderService(
          binderServices, Context.CONTENT_CAPTURE_MANAGER_SERVICE, IContentCaptureManager.class);
      addBinderService(binderServices, Context.ROLE_SERVICE, IRoleManager.class);
      addBinderService(binderServices, Context.ROLLBACK_SERVICE, IRollbackManager.class);
      addBinderService(binderServices, Context.THERMAL_SERVICE, IThermalService.class);
      addBinderService(binderServices, Context.BUGREPORT_SERVICE, IDumpstate.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= R) {
      addBinderService(binderServices, Context.APP_INTEGRITY_SERVICE, IAppIntegrityManager.class);
      addBinderService(binderServices, Context.AUTH_SERVICE, IAuthService.class);
      addBinderService(binderServices, Context.TETHERING_SERVICE, ITetheringConnector.class);
      addBinderService(binderServices, "telephony.registry", ITelephonyRegistry.class);
      addBinderService(binderServices, Context.PLATFORM_COMPAT_SERVICE, IPlatformCompat.class);
      addBinderService(binderServices, Context.FILE_INTEGRITY_SERVICE, IFileIntegrityService.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= S) {
      addBinderService(binderServices, "permissionmgr", IPermissionManager.class);
      addBinderService(
          binderServices, Context.TIME_ZONE_DETECTOR_SERVICE, ITimeZoneDetectorService.class);
      addBinderService(binderServices, Context.TIME_DETECTOR_SERVICE, ITimeDetectorService.class);
      addBinderService(
          binderServices, Context.SPEECH_RECOGNITION_SERVICE, IRecognitionServiceManager.class);
      addBinderService(
          binderServices, Context.LEGACY_PERMISSION_SERVICE, ILegacyPermissionManager.class);
      addBinderService(binderServices, Context.UWB_SERVICE, IUwbAdapter.class);
      addBinderService(binderServices, Context.VCN_MANAGEMENT_SERVICE, IVcnManagementService.class);
      addBinderService(
          binderServices, Context.TRANSLATION_MANAGER_SERVICE, ITranslationManager.class);
      addBinderService(binderServices, Context.SENSOR_PRIVACY_SERVICE, ISensorPrivacyManager.class);
      addBinderService(binderServices, Context.VPN_MANAGEMENT_SERVICE, IVpnManager.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= TIRAMISU) {
      addBinderService(
          binderServices, Context.AMBIENT_CONTEXT_SERVICE, IAmbientContextManager.class);
      addBinderService(binderServices, Context.LOCALE_SERVICE, ILocaleManager.class);
      addBinderService(binderServices, Context.SAFETY_CENTER_SERVICE, ISafetyCenterManager.class);
      addBinderService(binderServices, Context.STATUS_BAR_SERVICE, IStatusBar.class);
    }
    if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
      addBinderService(binderServices, Context.VIRTUAL_DEVICE_SERVICE, IVirtualDeviceManager.class);
      addBinderService(
          binderServices, Context.WEARABLE_SENSING_SERVICE, IWearableSensingManager.class);
    }

    return binderServices;
  }

  protected static void addBinderService(
      Map<String, BinderService> binderServices, String name, Class<? extends IInterface> clazz) {
    addBinderService(
        binderServices, name, clazz, clazz.getCanonicalName(), BinderProxyType.NULL, null);
  }

  private static void addBinderService(
      Map<String, BinderService> binderServices,
      String name,
      Class<? extends IInterface> clazz,
      BinderProxyType proxyType) {
    addBinderService(binderServices, name, clazz, clazz.getCanonicalName(), proxyType, null);
  }

  private static void addBinderService(
      Map<String, BinderService> binderServices, String name, String className) {
    Class<? extends IInterface> clazz;
    try {
      clazz = Class.forName(className).asSubclass(IInterface.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    addBinderService(binderServices, name, clazz, className, BinderProxyType.NULL, null);
  }

  private static void addBinderService(
      Map<String, BinderService> binderServices,
      String name,
      Class<? extends IInterface> clazz,
      BinderProxyType proxyType,
      @Nullable Object delegate) {
    addBinderService(binderServices, name, clazz, clazz.getCanonicalName(), proxyType, delegate);
  }

  private static void addBinderService(
      Map<String, BinderService> binderServices,
      String name,
      Class<? extends IInterface> clazz,
      String className,
      BinderProxyType proxyType,
      @Nullable Object delegate) {
    binderServices.put(name, new BinderService(clazz, className, proxyType, delegate));
  }

  /**
   * Returns the {@link IBinder} associated with the given system service. If the given service is
   * set to unavailable in {@link #setServiceAvailability}, {@code null} will be returned.
   */
  @Implementation
  protected static IBinder getService(String name) {
    synchronized (ShadowServiceManager.class) {
      if (unavailableServices.contains(name)) {
        return null;
      }
      return getBinderForService(name);
    }
  }

  @Implementation
  protected static void addService(String name, IBinder service) {}

  /**
   * Same as {@link #getService}.
   *
   * <p>The real implementation of {@link #checkService} differs from {@link #getService} in that it
   * is not a blocking call; so it is more likely to return {@code null} in cases where the service
   * isn't available (whereas {@link #getService} will block until it becomes available, until a
   * timeout or error happens).
   */
  @Implementation
  protected static IBinder checkService(String name) {
    synchronized (ShadowServiceManager.class) {
      if (unavailableServices.contains(name)) {
        return null;
      }
      return getBinderForService(name);
    }
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
   *
   * <p>A service is considered available by default.
   */
  public static synchronized void setServiceAvailability(String service, boolean available) {
    if (available) {
      unavailableServices.remove(service);
    } else {
      unavailableServices.add(service);
    }
  }

  @GuardedBy("ShadowServiceManager.class")
  @Nullable
  private static IBinder getBinderForService(String name) {
    BinderService binderService = binderServices.get(name);
    if (binderService == null) {
      return null;
    }
    return binderService.getBinder();
  }

  @Resetter
  public static synchronized void reset() {
    unavailableServices.clear();
  }
}
