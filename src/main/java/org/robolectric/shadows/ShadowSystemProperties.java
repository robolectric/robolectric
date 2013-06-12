package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Implements(value = Robolectric.Anything.class, className = "android.os.SystemProperties")
public class ShadowSystemProperties {
  private static final Map<String, Object> VALUES = new HashMap<String, Object>();
  private static final Set<String> alreadyWarned = new HashSet<String>();

  static {
    VALUES.put("ro.build.version.sdk", 8);
    VALUES.put("ro.debuggable", 0);
    VALUES.put("ro.secure", 1);
    VALUES.put("log.closeguard.Animation", false);
    VALUES.put("debug.choreographer.vsync", false); // disable vsync for Choreographer
  }

  @Implementation
  public static String get(String key) {
    Object o = VALUES.get(key);
    if (o == null) {
      warnUnknown(key);
      return null;
    }
    return o.toString();
  }

  @Implementation
  public static String get(String key, String def) {
    Object value = VALUES.get(key);
    return value == null ? def : value.toString();
  }

  @Implementation
  public static int getInt(String key, int def) {
    Object value = VALUES.get(key);
    return value == null ? def : (Integer) value;
  }

  @Implementation
  public static long getLong(String key, long def) {
    Object value = VALUES.get(key);
    return value == null ? def : (Long) value;
  }

  @Implementation
  public static boolean getBoolean(String key, boolean def) {
    Object value = VALUES.get(key);
    return value == null ? def : (Boolean) value;
  }

  synchronized private static void warnUnknown(String key) {
    if (alreadyWarned.add(key)) {
      System.err.println("WARNING: no system properties value for " + key);
    }
  }
}
