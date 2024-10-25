package org.robolectric.util;

import static com.google.common.base.StandardSystemProperty.OS_NAME;

import java.util.Locale;

/** OS-related utilities. */
public class OsUtil {

  private OsUtil() {}

  public static boolean isMac() {
    return osName().contains("mac");
  }

  public static boolean isWindows() {
    return osName().contains("win");
  }

  public static boolean isLinux() {
    return osName().contains("linux");
  }

  private static String osName() {
    return OS_NAME.value().toLowerCase(Locale.ROOT);
  }
}
