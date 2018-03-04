package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.content.ContentResolver;
import android.provider.Settings;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Settings.class)
public class ShadowSettings {
  @Implements(value = Settings.System.class)
  public static class ShadowSystem {
    private static final WeakHashMap<ContentResolver, Map<String, String>> dataMap = new WeakHashMap<>();

    @Implementation(minSdk = JELLY_BEAN_MR1)
    public static boolean putStringForUser(ContentResolver cr, String name, String value,
        int userHandle) {
      return putString(cr, name, value);
    }

    @Implementation(minSdk = JELLY_BEAN_MR1)
    public static String getStringForUser(ContentResolver cr, String name, int userHandle) {
      return getString(cr, name);
    }

    @Implementation
    public static boolean putString(ContentResolver cr, String name, String value) {
      get(cr).put(name, value);
      return true;
    }

    @Implementation
    public static String getString(ContentResolver cr, String name) {
      return get(cr).get(name);
    }

    private static Map<String, String> get(ContentResolver cr) {
      Map<String, String> map = dataMap.get(cr);
      if (map == null) {
        map = new HashMap<>();
        dataMap.put(cr, map);
      }
      return map;
    }
  }

  @Implements(value = Settings.Secure.class, inheritImplementationMethods = true)
  public static class ShadowSecure extends ShadowSystem {
  }

  @Implements(value = Settings.Global.class, inheritImplementationMethods = true, minSdk = JELLY_BEAN_MR1)
  public static class ShadowGlobal extends ShadowSystem {
  }

  /**
   * Sets the value of the {@link Settings.System#AIRPLANE_MODE_ON} setting.
   *
   * @param isAirplaneMode new status for airplane mode
   */
  public static void setAirplaneMode(boolean isAirplaneMode) {
    Settings.System.putInt(RuntimeEnvironment.application.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, isAirplaneMode ? 1 : 0);
  }

  /**
   * Non-Android accessor that allows the value of the WIFI_ON setting to be set.
   *
   * @param isOn new status for wifi mode
   */
  public static void setWifiOn(boolean isOn) {
    Settings.Secure.putInt(RuntimeEnvironment.application.getContentResolver(), Settings.Secure.WIFI_ON, isOn ? 1 : 0);
  }

  /**
   * Sets the value of the {@link Settings.System#TIME_12_24} setting.
   *
   * @param use24HourTimeFormat new status for the time setting
   */
  public static void set24HourTimeFormat(boolean use24HourTimeFormat) {
    Settings.System.putString(RuntimeEnvironment.application.getContentResolver(), Settings.System.TIME_12_24, use24HourTimeFormat ? "24" : "12");
  }
}
