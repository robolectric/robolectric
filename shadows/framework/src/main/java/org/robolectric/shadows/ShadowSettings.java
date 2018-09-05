package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
      get(cr).put(name, value);
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
      get(cr).put(name, value);
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
      get(cr).put(name, value);
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

  /**
   * Shadow for {@link Settings.Secure}
   */
  @Implements(value = Settings.Secure.class)
  public static class ShadowSecure {
    private static final WeakHashMap<ContentResolver, Map<String, Object>> dataMap =
        new WeakHashMap<ContentResolver, Map<String, Object>>();

    @Implementation(minSdk = JELLY_BEAN_MR1)
    protected static boolean setLocationProviderEnabledForUser(
        ContentResolver cr, String provider, boolean enabled, int uid) {
      return updateEnabledProviders(cr, provider, enabled);
    }

    @Implementation(maxSdk = JELLY_BEAN)
    protected static void setLocationProviderEnabled(
        ContentResolver cr, String provider, boolean enabled) {
      updateEnabledProviders(cr, provider, enabled);
    }

    private static boolean updateEnabledProviders(
        ContentResolver cr, String provider, boolean enabled) {
      Set<String> providers = new HashSet<>();
      String oldProviders =
          Settings.Secure.getString(cr, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
      if (!TextUtils.isEmpty(oldProviders)) {
        providers.addAll(Arrays.asList(oldProviders.split(",")));
      }

      if (enabled) {
        providers.add(provider);
      } else {
        providers.remove(provider);
      }

      String newProviders = TextUtils.join(",", providers.toArray());
      return Settings.Secure.putString(
          cr, Settings.Secure.LOCATION_PROVIDERS_ALLOWED, newProviders);
    }

    @Implementation
    protected static boolean putInt(ContentResolver resolver, String name, int value) {
      if (Settings.Secure.LOCATION_MODE.equals(name)
          && RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
        // Map LOCATION_MODE to underlying location provider storage API
        return Shadow.directlyOn(
            Settings.Secure.class,
            "setLocationModeForUser",
            ClassParameter.from(ContentResolver.class, resolver),
            ClassParameter.from(int.class, value),
            ClassParameter.from(int.class, 0));
      }
      get(resolver).put(name, value);
      return true;
    }

    @Implementation(minSdk = LOLLIPOP)
    public static boolean putIntForUser(
        ContentResolver cr, String name, int value, int userHandle) {
      putInt(cr, name, value);
      return true;
    }

    @Implementation
    protected static int getInt(ContentResolver resolver, String name)
        throws Settings.SettingNotFoundException {
      if (Settings.Secure.LOCATION_MODE.equals(name)
          && RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
        // Map from to underlying location provider storage API to location mode
        return Shadow.directlyOn(
            Settings.Secure.class,
            "getLocationModeForUser",
            ClassParameter.from(ContentResolver.class, resolver),
            ClassParameter.from(int.class, 0));
      }

      if (get(resolver).get(name) instanceof Integer) {
        return (Integer) get(resolver).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    protected static int getInt(ContentResolver resolver, String name, int def) {
      if (Settings.Secure.LOCATION_MODE.equals(name)
          && RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
        // Map from to underlying location provider storage API to location mode
        return Shadow.directlyOn(
            Settings.Secure.class,
            "getLocationModeForUser",
            ClassParameter.from(ContentResolver.class, resolver),
            ClassParameter.from(int.class, 0));
      }
      Integer v = (Integer) get(resolver).get(name);
      try {
        return v != null ? v : def;
      } catch (NumberFormatException e) {
        return def;
      }
    }

    @Implementation
    protected static boolean putString(ContentResolver cr, String name, String value) {
      get(cr).put(name, value);
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
      get(cr).put(name, value);
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

  /**
   * Shadow for {@link Settings.Global}
   */
  @Implements(value = Settings.Global.class, minSdk = JELLY_BEAN_MR1)
  public static class ShadowGlobal {
    private static final WeakHashMap<ContentResolver, Map<String, Object>> dataMap =
        new WeakHashMap<ContentResolver, Map<String, Object>>();

    @Implementation
    protected static boolean putInt(ContentResolver cr, String name, int value) {
      get(cr).put(name, value);
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
      get(cr).put(name, value);
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
      get(cr).put(name, value);
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

  /**
   * Sets the value of the {@link Settings.System#AIRPLANE_MODE_ON} setting.
   *
   * @param isAirplaneMode new status for airplane mode
   */
  public static void setAirplaneMode(boolean isAirplaneMode) {
    Settings.Global.putInt(
        RuntimeEnvironment.application.getContentResolver(),
        Settings.Global.AIRPLANE_MODE_ON,
        isAirplaneMode ? 1 : 0);
    Settings.System.putInt(
        RuntimeEnvironment.application.getContentResolver(),
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
        RuntimeEnvironment.application.getContentResolver(), Settings.Global.WIFI_ON, isOn ? 1 : 0);
    Settings.System.putInt(
        RuntimeEnvironment.application.getContentResolver(), Settings.System.WIFI_ON, isOn ? 1 : 0);
  }

  /**
   * Sets the value of the {@link Settings.System#TIME_12_24} setting.
   *
   * @param use24HourTimeFormat new status for the time setting
   */
  public static void set24HourTimeFormat(boolean use24HourTimeFormat) {
    Settings.System.putString(RuntimeEnvironment.application.getContentResolver(), Settings.System.TIME_12_24, use24HourTimeFormat ? "24" : "12");
  }

  private static boolean canDrawOverlays = false;

  /** @return `false` by default, or the value specified via {@link #setCanDrawOverlays(boolean)} */
  @Implementation(minSdk = M)
  protected static boolean canDrawOverlays(Context context) {
    return canDrawOverlays;
  }

  /** Sets the value returned by {@link #canDrawOverlays(Context)}. */
  public static void setCanDrawOverlays(boolean canDrawOverlays) {
    ShadowSettings.canDrawOverlays = canDrawOverlays;
  }

  @Resetter
  public static void reset() {
    canDrawOverlays = false;
  }
}
