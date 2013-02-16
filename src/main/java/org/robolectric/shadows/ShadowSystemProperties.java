package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Map;

@Implements(value = Robolectric.Anything.class, className = "android.os.SystemProperties")
public class ShadowSystemProperties {
    private static final Map<String, Object> VALUES = new HashMap<String, Object>();

    static {
        VALUES.put("ro.build.version.sdk", 12);
        VALUES.put("ro.debuggable", 0);
        VALUES.put("log.closeguard.Animation", false);
    }

    @Implementation
    public static String get(String key) {
      complain("SystemProperties.get(" + key + ")");
      return VALUES.get(key).toString();
    }

    @Implementation
    public static String get(String key, String def) {
      complain("SystemProperties.get(" + key + ", " + def + ")");
      Object value = VALUES.get(key);
        return value == null ? def : value.toString();
    }

    @Implementation
    public static int getInt(String key, int def) {
      complain("SystemProperties.getInt(" + key + ", " + def + ")");
      Object value = VALUES.get(key);
        return value == null ? def : (Integer) value;
    }

    @Implementation
    public static long getLong(String key, long def) {
      complain("SystemProperties.getLong(" + key + ", " + def + ")");
      Object value = VALUES.get(key);
        return value == null ? def : (Long) value;
    }

    @Implementation
    public static boolean getBoolean(String key, boolean def) {
        complain("SystemProperties.getBoolean(" + key + ", " + def + ")");
        Object value = VALUES.get(key);
        return value == null ? def : (Boolean) value;
    }

    private static void complain(String s) {
//        new RuntimeException(s).printStackTrace();
    }

}
