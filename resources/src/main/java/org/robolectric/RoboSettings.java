package org.robolectric;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that encapsulates reading global configuration options from the Java system properties
 * file.
 *
 * @deprecated Don't put more stuff here.
 */
@Deprecated
public class RoboSettings {

  private static final AtomicBoolean useGlobalScheduler = new AtomicBoolean(false);

  /**
   * @deprecated Use PAUSED looper mode.
   */
  @Deprecated
  public static boolean isUseGlobalScheduler() {
    return Boolean.getBoolean("robolectric.scheduling.global") || useGlobalScheduler.get();
  }

  /**
   * @deprecated Use PAUSED looper mode.
   */
  @Deprecated
  public static void setUseGlobalScheduler(boolean useGlobalScheduler) {
    RoboSettings.useGlobalScheduler.set(useGlobalScheduler);
  }
}
