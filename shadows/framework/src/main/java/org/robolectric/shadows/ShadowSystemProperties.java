package org.robolectric.shadows;

import android.os.SystemProperties;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = SystemProperties.class, isInAndroidSdk = false)
public class ShadowSystemProperties {
  private static Properties buildProperties = null;

  @Implementation
  public static String native_get(String key) {
    return native_get(key, "");
  }

  @Implementation
  public static String native_get(String key, String def) {
    String value = getProperty(key);
    return value == null ? def : value;
  }

  @Implementation
  public static int native_get_int(String key, int def) {
    String stringValue = getProperty(key);
    return stringValue == null ? def : Integer.parseInt(stringValue);
  }

  @Implementation
  public static long native_get_long(String key, long def) {
    String stringValue = getProperty(key);
    return stringValue == null ? def : Long.parseLong(stringValue);
  }

  @Implementation
  public static boolean native_get_boolean(String key, boolean def) {
    String stringValue = getProperty(key);
    if (stringValue == null) {
      return def;
    }

    switch (stringValue) {
      case "1":
      case "y":
      case "on":
      case "yes":
      case "true":
        return true;
      default:
        return false;
    }
  }

  @Implementation
  public static void native_set(String key, String val) {
    if (val == null) {
      loadProperties().remove(key);
    } else {
      loadProperties().setProperty(key, val);
    }
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

    // for backwards-compatibility reasons, set CPUS to unknown/ARM
    buildProperties.setProperty("ro.product.cpu.abi", "unknown");
    buildProperties.setProperty("ro.product.cpu.abi2", "unknown");
    buildProperties.setProperty("ro.product.cpu.abilist", "armeabi-v7a");
    buildProperties.setProperty("ro.product.cpu.abilist32", "armeabi-v7a,armeabi");
    buildProperties.setProperty("ro.product.cpu.abilist64", "armeabi-v7a,armeabi");
  }

  @Resetter
  public static synchronized void reset() {
    buildProperties = null;
  }
}
