package org.robolectric.shadows;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.view.Display;
import org.robolectric.Robolectric;
import org.robolectric.SdkConfig;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.HashMap;
import java.util.Map;

import static org.fest.reflect.core.Reflection.constructor;
import static org.robolectric.Robolectric.newInstanceOf;

@Implements(value = Robolectric.Anything.class, className = ShadowContextImpl.CLASS_NAME)
public class ShadowContextImpl extends ShadowContext {
  public static final String CLASS_NAME = "android.app.ContextImpl";

  private static final Map<String, String> SYSTEM_SERVICE_MAP = new HashMap<String, String>();

  static {
    // note that these are different!
    // They specify concrete classes within Robolectric for interfaces or abstract classes defined by Android
    SYSTEM_SERVICE_MAP.put(Context.WINDOW_SERVICE, "android.view.WindowManagerImpl");
    SYSTEM_SERVICE_MAP.put(Context.CLIPBOARD_SERVICE, "android.content.ClipboardManager");
    SYSTEM_SERVICE_MAP.put(Context.SENSOR_SERVICE, "android.hardware.TestSensorManager");
    SYSTEM_SERVICE_MAP.put(Context.VIBRATOR_SERVICE, "android.os.RoboVibrator");

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
  }

  @RealObject private Context realContextImpl;
  private Map<String, Object> systemServices = new HashMap<String, Object>();
  private final SdkConfig sdkConfig;

  public ShadowContextImpl(SdkConfig sdkConfig) {
    this.sdkConfig = sdkConfig;
  }

  @Implements(value = Robolectric.Anything.class, className = ShadowServiceFetcher.CLASS_NAME, looseSignatures = true)
  public static class ShadowServiceFetcher {
    public static final String CLASS_NAME = "android.app.ContextImpl$ServiceFetcher";

    @Implementation
    public Object createService(Object ctx) {
      return null;
    }
  }

  @Implementation
  public Object getSystemService(String name) {
    if (name.equals(Context.LAYOUT_INFLATER_SERVICE)) {
      return new RoboLayoutInflater(realContextImpl);
    }

    Object service = systemServices.get(name);
    if (service == null) {
      String serviceClassName = SYSTEM_SERVICE_MAP.get(name);
      if (serviceClassName == null) {
        System.err.println("WARNING: unknown service " + name);
        return null;
      }

      try {
        if (serviceClassName.equals("android.app.SearchManager")) {
          service = constructor().withParameterTypes(Context.class, Handler.class).in(SearchManager.class).newInstance(realContextImpl, null);
        } else if (serviceClassName.equals("android.os.storage.StorageManager")) {
          service = constructor().in(StorageManager.class).newInstance();
        } else if ((sdkConfig.getApiLevel() >= Build.VERSION_CODES.JELLY_BEAN_MR1) && (serviceClassName.equals("android.view.WindowManagerImpl"))) {
          Display display = newInstanceOf(Display.class);
          service = constructor().withParameterTypes(Display.class).in(Class.forName("android.view.WindowManagerImpl")).newInstance(display);
        } else {
          service = newInstanceOf(Class.forName(serviceClassName));
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
}
