package org.robolectric.shadows;

import android.content.ContentResolver;
import android.provider.Settings;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Shadow of {@code Settings} that allows the status of various System and Secure settings to be simulated, changed and
 * queried.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Settings.class)
public class ShadowSettings {
  @Implements(Settings.System.class)
  public static class SettingsImpl {
    private static final WeakHashMap<ContentResolver, Map<String, Object>> dataMap = new WeakHashMap<ContentResolver, Map<String, Object>>();

    @Implementation
    public static boolean putInt(ContentResolver cr, String name, int value) {
      get(cr).put(name, value);
      return true;
    }

    @Implementation
    public static int getInt(ContentResolver cr, String name, int def) {
      if (get(cr).get(name) instanceof Integer) {
        return (Integer) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    public static int getInt(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Integer) {
        return (Integer) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    public static boolean putString(ContentResolver cr, String name, String value) {
      get(cr).put(name, value);
      return true;
    }

    @Implementation
    public static String getString(ContentResolver cr, String name) {
      if (get(cr).get(name) instanceof String) {
        return (String) get(cr).get(name);
      } else {
        return null;
      }
    }

    @Implementation
    public static boolean putLong(ContentResolver cr, String name, long value) {
      get(cr).put(name, value);
      return true;
    }

    @Implementation
    public static long getLong(ContentResolver cr, String name, long def) {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    public static long getLong(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Long) {
        return (Long) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    public static boolean putFloat(ContentResolver cr, String name, float value) {
      get(cr).put(name, value);
      return true;
    }

    @Implementation
    public static float getFloat(ContentResolver cr, String name, float def) {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        return def;
      }
    }

    @Implementation
    public static float getFloat(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
      if (get(cr).get(name) instanceof Float) {
        return (Float) get(cr).get(name);
      } else {
        throw new Settings.SettingNotFoundException(name);
      }
    }

    @Implementation
    private static Map<String, Object> get(ContentResolver cr) {
      Map<String, Object> map = dataMap.get(cr);
      if (map == null) {
        map = new HashMap<String, Object>();
        dataMap.put(cr, map);
      }
      return map;
    }
  }

  @Implements(value = Settings.System.class, inheritImplementationMethods = true)
  public static class ShadowSystem extends SettingsImpl {
  }

  @Implements(value = Settings.Secure.class, inheritImplementationMethods = true)
  public static class ShadowSecure extends SettingsImpl {
  }

  @Implements(value = Settings.Global.class, inheritImplementationMethods = true)
  public static class ShadowGlobal extends SettingsImpl {
  }

  /**
   * Non-Android accessor that allows the value of the AIRPLANE_MODE_ON setting to be set.
   *
   * @param isAirplaneMode new status for airplane mode
   */
  public static void setAirplaneMode(boolean isAirplaneMode) {
    Settings.System.putInt(Robolectric.application.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, isAirplaneMode ? 1 : 0);
  }

  /**
   * Non-Android accessor that allows the value of the WIFI_ON setting to be set.
   *
   * @param isOn new status for wifi mode
   */
  public static void setWifiOn(boolean isOn) {
    Settings.Secure.putInt(Robolectric.application.getContentResolver(), Settings.Secure.WIFI_ON, isOn ? 1 : 0);
  }

  /**
   * Non-Android accessor thatallows the value of the TIME_12_24 setting to be set.
   *
   * @param use24HourTimeFormat new status for the time setting
   */
  public static void set24HourTimeFormat(boolean use24HourTimeFormat) {
    Settings.System.putInt(Robolectric.application.getContentResolver(), Settings.System.TIME_12_24, use24HourTimeFormat ? 24 : 12);
  }
}
