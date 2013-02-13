package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Map;

@Implements(Robolectric.Anything.class)
public class ShadowSystemProperties {
    private static final Map<String, Object> VALUES = new HashMap<String, Object>();

    static {
        VALUES.put("ro.build.version.sdk", 12);
        VALUES.put("ro.debuggable", 0);
        VALUES.put("log.closeguard.Animation", false);
    }

    @Implementation
    public static String get(String key) {
        new RuntimeException("SystemProperties.get(" + key + ")").printStackTrace();
        return VALUES.get(key).toString();
    }

    @Implementation
    public static String get(String key, String def) {
        new RuntimeException("SystemProperties.get(" + key + ", " + def + ")").printStackTrace();
        Object value = VALUES.get(key);
        return value == null ? def : value.toString();
    }

    @Implementation
    public static int getInt(String key, int def) {
        new RuntimeException("SystemProperties.getInt(" + key + ", " + def + ")").printStackTrace();
        Object value = VALUES.get(key);
        return value == null ? def : (Integer) value;
    }

    @Implementation
    public static long getLong(String key, long def) {
        new RuntimeException("SystemProperties.getLong(" + key + ", " + def + ")").printStackTrace();
        Object value = VALUES.get(key);
        return value == null ? def : (Long) value;
    }

    @Implementation
    public static boolean getBoolean(String key, boolean def) {
        new RuntimeException("SystemProperties.getBoolean(" + key + ", " + def + ")").printStackTrace();
        Object value = VALUES.get(key);
        return value == null ? def : (Boolean) value;
    }

}
