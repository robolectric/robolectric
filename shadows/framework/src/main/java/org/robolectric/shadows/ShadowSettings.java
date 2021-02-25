package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.provider.Settings.Secure.LOCATION_MODE_OFF;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Settings.class)
public class ShadowSettings {

  @Implements(value = Settings.System.class)
  public static class ShadowSystem {
    private static final WeakHashMap<ContentResolver, Map<String, Object>> dataMap = new WeakHashMap<ContentResolver, Map<String, Object>>();

    @Implementation
    protected static boolean putInt(ContentResolver cr, String name, int value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.System.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static int getInt(ContentResolver cr, String name, int def) {
      if (get(cr).get(name) instanceof Integer) {
        return (Integer) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static int getInt(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Integer) {
        return (Integer) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    protected static boolean putString(ContentResolver cr, String name, String value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.System.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static String getString(ContentResolver cr, String name) {
      if (get(cr).get(name) instanceof String) {
        return (String) get(cr).get(name);
      } else {
        return null;
      }
    }

    @Implementation(minSdk = JELLY_BEAN_MR1)
    protected static String getStringForUser(ContentResolver cr, String name, int userHandle) {
      return getString(cr, name);
    }

    @Implementation
    protected static boolean putLong(ContentResolver cr, String name, long value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.System.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static long getLong(ContentResolver cr, String name, long def) {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static long getLong(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    protected static boolean putFloat(ContentResolver cr, String name, float value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.System.getUriFor(name), null);
        }
      }
      get(cr).put(name, value);
      return true;
    }

    @Implementation
    protected static float getFloat(ContentResolver cr, String name, float def) {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static float getFloat(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    private static Map<String, Object> get(ContentResolver cr) {
      Map<String, Object> map = dataMap.get(cr);
      if (map == null) {
        map = new HashMap<>();
        dataMap.put(cr, map);
      }
      return map;
    }
  }

  @Implements(value = Settings.Secure.class)
  public static class ShadowSecure {
    private static final WeakHashMap<ContentResolver, Map<String, Object>> dataMap =
        new WeakHashMap<ContentResolver, Map<String, Object>>();

    private static final HashMap<String, Object> SECURE_DEFAULTS = new HashMap<>();

    // source of truth for initial location state
    static final boolean INITIAL_GPS_PROVIDER_STATE = true;
    static final boolean INITIAL_NETWORK_PROVIDER_STATE = false;

    static {
      if (INITIAL_GPS_PROVIDER_STATE && INITIAL_NETWORK_PROVIDER_STATE) {
        SECURE_DEFAULTS.put(Secure.LOCATION_MODE, Secure.LOCATION_MODE_HIGH_ACCURACY);
        SECURE_DEFAULTS.put(Secure.LOCATION_PROVIDERS_ALLOWED, "gps,network");
      } else if (INITIAL_GPS_PROVIDER_STATE) {
        SECURE_DEFAULTS.put(Secure.LOCATION_MODE, Secure.LOCATION_MODE_SENSORS_ONLY);
        SECURE_DEFAULTS.put(Secure.LOCATION_PROVIDERS_ALLOWED, "gps");
      } else if (INITIAL_NETWORK_PROVIDER_STATE) {
        SECURE_DEFAULTS.put(Secure.LOCATION_MODE, Secure.LOCATION_MODE_BATTERY_SAVING);
        SECURE_DEFAULTS.put(Secure.LOCATION_PROVIDERS_ALLOWED, "network");
      } else {
        SECURE_DEFAULTS.put(Secure.LOCATION_MODE, LOCATION_MODE_OFF);
      }
    }

    @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = P)
    @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
    protected static boolean setLocationProviderEnabledForUser(
        ContentResolver cr, String provider, boolean enabled, int uid) {
      return updateEnabledProviders(cr, provider, enabled);
    }

    @Implementation(maxSdk = JELLY_BEAN)
    protected static void setLocationProviderEnabled(
        ContentResolver cr, String provider, boolean enabled) {
      updateEnabledProviders(cr, provider, enabled);
    }

    // only for use locally and by ShadowLocationManager, which requires a tight integration with
    // ShadowSettings due to historical weirdness between LocationManager and Settings.
    static boolean updateEnabledProviders(ContentResolver cr, String provider, boolean enabled) {
      Set<String> providers = new HashSet<>();
      String oldProviders =
          Settings.Secure.getString(cr, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
      if (!TextUtils.isEmpty(oldProviders)) {
        providers.addAll(Arrays.asList(oldProviders.split(",")));
      }

      if (enabled == oldProviders.contains(provider)) {
        return true;
      }

      if (enabled) {
        providers.add(provider);
      } else {
        providers.remove(provider);
      }

      String newProviders = TextUtils.join(",", providers.toArray());
      boolean r =
          Settings.Secure.putString(cr, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newProviders);

      Intent providersBroadcast = new Intent(LocationManager.PROVIDERS_CHANGED_ACTION);
      if (RuntimeEnvironment.getApiLevel() >= Q) {
        providersBroadcast.putExtra(LocationManager.EXTRA_PROVIDER_NAME, provider);
      }
      if (RuntimeEnvironment.getApiLevel() >= R) {
        providersBroadcast.putExtra(LocationManager.EXTRA_PROVIDER_ENABLED, enabled);
      }
      RuntimeEnvironment.getApplication().sendBroadcast(providersBroadcast);

      return r;
    }

    @Implementation
    protected static boolean putInt(ContentResolver cr, String name, int value) {
      boolean changed = !Objects.equals(get(cr).put(name, value), value);

      if (Settings.Secure.LOCATION_MODE.equals(name)) {
        if (RuntimeEnvironment.getApiLevel() <= P) {
          // do this after setting location mode but before invoking contentobservers, so that
          // observers for both settings will see the correct values
          boolean gps =
              (value == Settings.Secure.LOCATION_MODE_SENSORS_ONLY
                  || value == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
          boolean network =
              (value == Settings.Secure.LOCATION_MODE_BATTERY_SAVING
                  || value == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
          Settings.Secure.setLocationProviderEnabled(cr, LocationManager.GPS_PROVIDER, gps);
          Settings.Secure.setLocationProviderEnabled(cr, LocationManager.NETWORK_PROVIDER, network);
        }

        Intent modeBroadcast = new Intent(LocationManager.MODE_CHANGED_ACTION);
        if (RuntimeEnvironment.getApiLevel() >= R) {
          modeBroadcast.putExtra(
              LocationManager.EXTRA_LOCATION_ENABLED, value != LOCATION_MODE_OFF);
        }
        RuntimeEnvironment.getApplication().sendBroadcast(modeBroadcast);
      }

      if (changed && cr != null) {
        cr.notifyChange(Settings.Secure.getUriFor(name), null);
      }

      return true;
    }

    @Implementation(minSdk = LOLLIPOP)
    protected static boolean putIntForUser(
        ContentResolver cr, String name, int value, int userHandle) {
      putInt(cr, name, value);
      return true;
    }

    @Implementation
    protected static int getInt(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (Settings.Secure.LOCATION_MODE.equals(name)
          && RuntimeEnvironment.getApiLevel() >= KITKAT
          && RuntimeEnvironment.getApiLevel() < P) {
        // Map from to underlying location provider storage API to location mode
        return Shadow.directlyOn(
            Settings.Secure.class,
            "getLocationModeForUser",
            ClassParameter.from(ContentResolver.class, cr),
            ClassParameter.from(int.class, 0));
      }

      if (get(cr).get(name) instanceof Integer) {
        return (Integer) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    protected static int getInt(ContentResolver cr, String name, int def) {
      if (Settings.Secure.LOCATION_MODE.equals(name)
          && RuntimeEnvironment.getApiLevel() >= KITKAT
          && RuntimeEnvironment.getApiLevel() < P) {
        // Map from to underlying location provider storage API to location mode
        return Shadow.directlyOn(
            Settings.Secure.class,
            "getLocationModeForUser",
            ClassParameter.from(ContentResolver.class, cr),
            ClassParameter.from(int.class, 0));
      }
      Integer v = (Integer) get(cr).get(name);
      try {
        return v != null ? v : def;
      } catch (NumberFormatException e) {
        return def;
      }
    }

    @Implementation
    protected static boolean putString(ContentResolver cr, String name, String value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.Secure.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static String getString(ContentResolver cr, String name) {
      if (get(cr).get(name) instanceof String) {
        return (String) get(cr).get(name);
      } else {
        return null;
      }
    }

    @Implementation(minSdk = JELLY_BEAN_MR1)
    protected static String getStringForUser(ContentResolver cr, String name, int userHandle) {
      return getString(cr, name);
    }

    @Implementation
    protected static boolean putLong(ContentResolver cr, String name, long value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.Secure.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static long getLong(ContentResolver cr, String name, long def) {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static long getLong(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    protected static boolean putFloat(ContentResolver cr, String name, float value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.Secure.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static float getFloat(ContentResolver cr, String name, float def) {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static float getFloat(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    private static Map<String, Object> get(ContentResolver cr) {
      Map<String, Object> map = dataMap.get(cr);
      if (map == null) {
        map = new HashMap<>(SECURE_DEFAULTS);
        dataMap.put(cr, map);
      }
      return map;
    }
  }

  @Implements(value = Settings.Global.class, minSdk = JELLY_BEAN_MR1)
  public static class ShadowGlobal {
    private static final WeakHashMap<ContentResolver, Map<String, Object>> dataMap =
        new WeakHashMap<ContentResolver, Map<String, Object>>();

    @Implementation
    protected static boolean putInt(ContentResolver cr, String name, int value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.Global.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static int getInt(ContentResolver cr, String name, int def) {
      if (get(cr).get(name) instanceof Integer) {
        return (Integer) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static int getInt(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Integer) {
        return (Integer) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    protected static boolean putString(ContentResolver cr, String name, String value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.Global.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static String getString(ContentResolver cr, String name) {
      if (get(cr).get(name) instanceof String) {
        return (String) get(cr).get(name);
      } else {
        return null;
      }
    }

    @Implementation(minSdk = JELLY_BEAN_MR1)
    protected static String getStringForUser(ContentResolver cr, String name, int userHandle) {
      return getString(cr, name);
    }

    @Implementation
    protected static boolean putLong(ContentResolver cr, String name, long value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.Global.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static long getLong(ContentResolver cr, String name, long def) {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static long getLong(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    protected static boolean putFloat(ContentResolver cr, String name, float value) {
      if (!Objects.equals(get(cr).put(name, value), value)) {
        if (cr != null) {
          cr.notifyChange(Settings.Global.getUriFor(name), null);
        }
      }
      return true;
    }

    @Implementation
    protected static float getFloat(ContentResolver cr, String name, float def) {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    protected static float getFloat(ContentResolver cr, String name)
        throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    private static Map<String, Object> get(ContentResolver cr) {
      Map<String, Object> map = dataMap.get(cr);
      if (map == null) {
        map = new HashMap<>();
        dataMap.put(cr, map);
      }
      return map;
    }
  }

  /**
   * Sets the value of the {@link Settings.System#AIRPLANE_MODE_ON} setting.
   *
   * @param isAirplaneMode new status for airplane mode
   */
  public static void setAirplaneMode(boolean isAirplaneMode) {
    Settings.Global.putInt(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.Global.AIRPLANE_MODE_ON,
        isAirplaneMode ? 1 : 0);
    Settings.System.putInt(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.System.AIRPLANE_MODE_ON,
        isAirplaneMode ? 1 : 0);
  }

  /**
   * Non-Android accessor that allows the value of the WIFI_ON setting to be set.
   *
   * @param isOn new status for wifi mode
   */
  public static void setWifiOn(boolean isOn) {
    Settings.Global.putInt(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.Global.WIFI_ON,
        isOn ? 1 : 0);
    Settings.System.putInt(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.System.WIFI_ON,
        isOn ? 1 : 0);
  }

  /**
   * Sets the value of the {@link Settings.System#TIME_12_24} setting.
   *
   * @param use24HourTimeFormat new status for the time setting
   */
  public static void set24HourTimeFormat(boolean use24HourTimeFormat) {
    Settings.System.putString(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.System.TIME_12_24,
        use24HourTimeFormat ? "24" : "12");
  }

  private static boolean canDrawOverlays = false;

  /** @return false by default, or the value specified via {@link #setCanDrawOverlays(boolean)} */
  @Implementation(minSdk = M)
  protected static boolean canDrawOverlays(Context context) {
    return canDrawOverlays;
  }

  /** Sets the value returned by {@link #canDrawOverlays(Context)}. */
  public static void setCanDrawOverlays(boolean canDrawOverlays) {
    ShadowSettings.canDrawOverlays = canDrawOverlays;
  }

  /**
   * Sets the value of the {@link Settings.Global#ADB_ENABLED} setting or {@link
   * Settings.Secure#ADB_ENABLED} depending on API level.
   *
   * @param adbEnabled new value for whether adb is enabled
   */
  public static void setAdbEnabled(boolean adbEnabled) {
    // This setting moved from Secure to Global in JELLY_BEAN_MR1
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      Settings.Global.putInt(
          RuntimeEnvironment.getApplication().getContentResolver(),
          Settings.Global.ADB_ENABLED,
          adbEnabled ? 1 : 0);
    }
    // Support all clients by always setting the Secure version of the setting
    Settings.Secure.putInt(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.Secure.ADB_ENABLED,
        adbEnabled ? 1 : 0);
  }

  /**
   * Sets the value of the {@link Settings.Global#INSTALL_NON_MARKET_APPS} setting or {@link
   * Settings.Secure#INSTALL_NON_MARKET_APPS} depending on API level.
   *
   * @param installNonMarketApps new value for whether non-market apps are allowed to be installed
   */
  public static void setInstallNonMarketApps(boolean installNonMarketApps) {
    // This setting moved from Secure to Global in JELLY_BEAN_MR1 and then moved it back to Global
    // in LOLLIPOP. Support all clients by always setting this field on all versions >=
    // JELLY_BEAN_MR1.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      Settings.Global.putInt(
          RuntimeEnvironment.getApplication().getContentResolver(),
          Settings.Global.INSTALL_NON_MARKET_APPS,
          installNonMarketApps ? 1 : 0);
    }
    // Always set the Secure version of the setting
    Settings.Secure.putInt(
        RuntimeEnvironment.getApplication().getContentResolver(),
        Settings.Secure.INSTALL_NON_MARKET_APPS,
        installNonMarketApps ? 1 : 0);
  }

  @Resetter
  public static void reset() {
    canDrawOverlays = false;
  }
}
