package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;

import android.app.IAlarmManager;
import android.app.ISearchManager;
import android.app.admin.IDevicePolicyManager;
import android.app.trust.ITrustManager;
import android.content.Context;
import android.content.IRestrictionsManager;
import android.location.ILocationManager;
import android.net.IConnectivityManager;
import android.net.wifi.IWifiManager;
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
          if (RuntimeEnvironment.getApiLevel() < O) {
            put(
                "mount",
                createBinder("android.os.storage.IMountService", "android.os.storage.IMountService"));
          } else {
            put(
                "mount",
                createBinder(IStorageManager.class, "android.os.storage.IStorageManager"));
          }
          put(
              Context.LOCATION_SERVICE,
              createBinder(ILocationManager.class, "android.location.ILocationManager"));
          put(
                  Context.INPUT_METHOD_SERVICE,
              createBinder(IInputMethodManager .class, "com.android.internal.view.IInputMethodManager"));
          put(
              Context.ALARM_SERVICE,
              createBinder(IAlarmManager.class, "android.app.IAlarmManager"));
          put(
              Context.POWER_SERVICE,
              createBinder(IPowerManager.class, "android.os.IPowerManager"));
          put(
              Context.USER_SERVICE,
              createBinder(IUserManager.class, "android.os.IUserManager"));
          put(
              Context.RESTRICTIONS_SERVICE,
              createBinder(IRestrictionsManager.class, "android.content.IRestrictionsManager"));
          put(
              BatteryStats.SERVICE_NAME,
              createBinder(IBatteryStats.class, "com.android.internal.app.IBatteryStats"));
          put(
              "batteryproperties",
              createBinder(IBatteryPropertiesRegistrar.class, "android.os.IBatteryPropertiesRegistrar"));
          put(
              Context.DROPBOX_SERVICE,
              createBinder(IDropBoxManagerService.class, "com.android.internal.os.IDropBoxManagerService"));
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
          if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
            put(
                Context.TRUST_SERVICE,
                createBinder(ITrustManager.class, "android.app.trust.ITrustManager"));
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
