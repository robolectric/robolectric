package org.robolectric;

/**
 * Class that encapsulates reading global configuration options from the Java system properties
 * file.
 *
 * @deprecated Don't put more stuff here.
 */
@Deprecated
public class RoboSettings {

  private static boolean useGlobalScheduler;

  static {
    useGlobalScheduler = Boolean.getBoolean("robolectric.scheduling.global");
  }

  /**
   * @deprecated Use PAUSED looper mode.
   */
  @Deprecated
  public static boolean isUseGlobalScheduler() {
    return useGlobalScheduler;
  }

  /**
   * @deprecated Use PAUSED looper mode.
   */
  @Deprecated
  public static void setUseGlobalScheduler(boolean useGlobalScheduler) {
    RoboSettings.useGlobalScheduler = useGlobalScheduler;
  }
}
