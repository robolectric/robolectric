package org.robolectric.shadows;

import android.os.SystemProperties;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = SystemProperties.class, isInAndroidSdk = false)
public class ShadowSystemProperties {
  private static Properties buildProperties = null;
  private static final Set<String> alreadyWarned = new HashSet<>();


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
    return stringValue == null ? def : Boolean.parseBoolean(stringValue);
  }

  @Implementation
  public static void native_set(String key, String val) {
    loadProperties().setProperty(key, val);
  }

  // ignored/unimplemented methods
  // private static native void native_add_change_callback();
  // private static native void native_report_sysprop_change();

  private synchronized static String getProperty(String key) {
    return loadProperties().getProperty(key);
  }

  private synchronized static Properties loadProperties() {
    if (buildProperties == null) {
      // load the prop from classpath
      ClassLoader cl = SystemProperties.class.getClassLoader();
      URL urlFromCl = cl.getResource("build.prop");
      try (InputStream is = cl.getResourceAsStream("build.prop")) {
        Preconditions.checkNotNull(is, "could not find build.prop");
        buildProperties = new Properties();
        buildProperties.load(is);
      } catch (IOException e) {
        throw new RuntimeException("failed to load build.prop", e);
      }
  /**
   * Set the value of a system property.
   *
   * <p>For setting android.os.Build.X values, its recommended to use {@link ShadowBuild} instead.
   * Most Build values are loaded statically at initialization, so calling setProperty to set a
   * Build flag (like Build.ID) from your test may have no effect.
   */
  public static void setProperty(String key, String value) {
    VALUES.put(key, value);
  }

  /** Removes a system property */
  public static void removeProperty(String key) {
    VALUES.remove(key);
  }

  synchronized private static void warnUnknown(String key) {
    if (alreadyWarned.add(key)) {
      System.err.println("WARNING: no system properties value for " + key);
    }
    return buildProperties;
  }

  @Resetter
  public static void reset() {
    buildProperties = null;
  }

  @Resetter
  public static void reset() {
    setDefaults();
  }
}
