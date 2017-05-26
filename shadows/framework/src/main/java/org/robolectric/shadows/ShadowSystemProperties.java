package org.robolectric.shadows;

import android.os.SystemProperties;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Implements(value = SystemProperties.class, isInAndroidSdk = false)
public class ShadowSystemProperties {
  private static final Map<String, String> INITIAL_VALUES = new HashMap<String, String>() {{
    put("ro.build.version.release", "2.2");
    put("ro.build.version.incremental", "0");
    put("ro.build.version.sdk", "8");
    put("ro.build.date.utc", "1277708400000");  // Jun 28, 2010
    put("ro.debuggable", "0");
    put("ro.secure", "1");
    put("ro.product.cpu.abilist", "armeabi-v7a");
    put("ro.product.cpu.abilist32", "armeabi-v7a,armeabi");
    put("ro.product.cpu.abilist64", "armeabi-v7a,armeabi");
    put("ro.build.fingerprint", "robolectric");
    put("ro.build.version.all_codenames", "REL");
    put("log.closeguard.Animation", "n");
    put("debug.choreographer.vsync", "n"); // disable vsync for Choreographer
  }};

  private static final Map<String, String> VALUES = new HashMap<>();
  private static final Set<String> alreadyWarned = new HashSet<>();

  static {
    VALUES.putAll(INITIAL_VALUES);
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(SystemProperties.class, "sChangeCallbacks", new ArrayList());

    VALUES.clear();
    VALUES.putAll(INITIAL_VALUES);
  }

  @Implementation
  public static String get(String key) {
    Object o = VALUES.get(key);
    if (o == null) {
      warnUnknown(key);
      return "";
    }
    return o.toString();
  }

  synchronized private static String getValue(String key) {
    if (key == null) {
      throw new NullPointerException("key must not be null");
    }

    String value = VALUES.get(key);
    if (value == null) {
      warnUnknown(key);
      return "";
    } else {
      return value;
    }
  }

  synchronized private static void putValue(String key, String value) {
    VALUES.put(key, value);
  }

  synchronized private static void warnUnknown(String key) {
    if (alreadyWarned.add(key)) {
      System.err.println("WARNING: no system properties value for " + key);
    }
  }

  @Implementation
  public static String native_get(String key) {
    return native_get(key, null);
  }

  @Implementation
  public static String native_get(String key, String defaultValue) {
    String value = getValue(key);
    if (value.isEmpty()) {
      value = defaultValue;
    }
    if (value.isEmpty()) {
      value = "";
    }
    return value;
  }

  @Implementation
  public static int native_get_int(String key, int defaultValue) {
    String value = getValue(key);
    try {
      if (value.length() > 0) {
        return Integer.parseInt(value);
      }
    } catch (NumberFormatException e) {
      // ok, fall through
    }
    return defaultValue;
  }

  @Implementation
  public static long native_get_long(String key, long defaultValue) {
    String value = getValue(key);
    try {
      if (value.length() > 0) {
        return Long.parseLong(value);
      }
    } catch (NumberFormatException e) {
      // ok, fall through
    }
    return defaultValue;
  }

  @Implementation
  public static boolean native_get_boolean(String key, boolean defaultValue) {
    String value = getValue(key);
    if (value.length() == 1) {
      if (value.equals("0") || value.equals("n")) {
        return false;
      } else if (value.equals("1") || value.equals("y")) {
        return true;
      }
    } else if (value.length() > 1) {
      if (value.equals("no") || value.equals("false") || value.equals("off")) {
        return false;
      } else if (value.equals("yes") || value.equals("true") || value.equals("on")) {
        return true;
      }
    }
    return defaultValue;
  }

  @Implementation
  synchronized public static void native_set(String key, String value) {
    putValue(key, value);

    native_report_sysprop_change();
  }

  @Implementation
  public static void native_report_sysprop_change() {
    ReflectionHelpers.callStaticMethod(SystemProperties.class, "callChangeCallbacks");
  }
}
