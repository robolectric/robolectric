package org.robolectric.shadows;

import android.os.SystemProperties;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = SystemProperties.class, isInAndroidSdk = false)
public class ShadowSystemProperties {
  private static Properties buildProperties = null;

  @Implementation
  protected static String native_get(String key) {
    return native_get(key, "");
  }

  @Implementation
  protected static String native_get(String key, String def) {
    String value = getProperty(key);
    return value == null ? def : value;
  }

  @Implementation
  protected static int native_get_int(String key, int def) {
    String stringValue = getProperty(key);
    return stringValue == null ? def : Integer.parseInt(stringValue);
  }

  @Implementation
  protected static long native_get_long(String key, long def) {
    String stringValue = getProperty(key);
    return stringValue == null ? def : Long.parseLong(stringValue);
  }

  @Implementation
  protected static boolean native_get_boolean(String key, boolean def) {
    String stringValue = getProperty(key);
    if ("1".equals(stringValue)
        || "y".equals(stringValue)
        || "yes".equals(stringValue)
        || "on".equals(stringValue)
        || "true".equals(stringValue)) {
      return true;
    }
    if ("0".equals(stringValue)
        || "n".equals(stringValue)
        || "no".equals(stringValue)
        || "off".equals(stringValue)
        || "false".equals(stringValue)) {
      return false;
    }
    return def;
  }

  @Implementation
  protected static void native_set(String key, String val) {
    if (val == null) {
      loadProperties().remove(key);
    } else {
      loadProperties().setProperty(key, val);
    }
  }

  /**
   * Overrides the system property for testing. Similar to the Android implementation, the value may
   * be coerced to other types like boolean or long depending on the get method that is used.
   *
   * <p>Note: Use {@link org.robolectric.shadows.ShadowBuild} instead for changing fields in {@link
   * android.os.Build}.
   */
  public static void override(String key, String val) {
    SystemProperties.set(key, val);
  }

  // ignored/unimplemented methods
  // private static native void native_add_change_callback();
  // private static native void native_report_sysprop_change();

  private static synchronized String getProperty(String key) {
    return loadProperties().getProperty(key);
  }

  private static synchronized Properties loadProperties() {
    if (buildProperties == null) {
      // load the prop from classpath
      ClassLoader cl = SystemProperties.class.getClassLoader();
      try (InputStream is = cl.getResourceAsStream("build.prop")) {
        Preconditions.checkNotNull(is, "could not find build.prop");
        buildProperties = new Properties();
        buildProperties.load(is);
        setDefaults(buildProperties);
      } catch (IOException e) {
        throw new RuntimeException("failed to load build.prop", e);
      }
    }
    return buildProperties;
  }

  private static void setDefaults(Properties buildProperties) {
    // The default generated build.prop can make this look like the emulator.
    // Override common default properties to indicate platform is robolectric
    // TODO: put these values directly in build.prop generated from build system
    buildProperties.setProperty("ro.build.fingerprint", "robolectric");
    buildProperties.setProperty("ro.product.device", "robolectric");
    buildProperties.setProperty("ro.product.name", "robolectric");
    buildProperties.setProperty("ro.product.model", "robolectric");
    buildProperties.setProperty("ro.hardware", "robolectric");
    buildProperties.setProperty("ro.build.characteristics", "robolectric");

    // for backwards-compatiblity reasons, set CPUS to unknown/ARM
    buildProperties.setProperty("ro.product.cpu.abi", "unknown");
    buildProperties.setProperty("ro.product.cpu.abi2", "unknown");
    buildProperties.setProperty("ro.product.cpu.abilist", "armeabi-v7a");
    buildProperties.setProperty("ro.product.cpu.abilist32", "armeabi-v7a,armeabi");
    buildProperties.setProperty("ro.product.cpu.abilist64", "armeabi-v7a,armeabi");

    // Update SQLite sync mode and journal mode defaults for faster SQLite operations due to less
    // file I/O.
    buildProperties.setProperty("debug.sqlite.syncmode", "OFF");
    buildProperties.setProperty("debug.sqlite.wal.syncmode", "OFF");
    buildProperties.setProperty("debug.sqlite.journalmode", "MEMORY");
  }

  @Resetter
  public static synchronized void reset() {
    ReflectionHelpers.setStaticField(SystemProperties.class, "sChangeCallbacks", new ArrayList<>());
    buildProperties = null;
  }
}
