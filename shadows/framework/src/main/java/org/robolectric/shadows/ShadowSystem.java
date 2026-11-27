package org.robolectric.shadows;

import android.os.SystemClock;
import org.robolectric.annotation.LooperMode;

public class ShadowSystem {

  /**
   * Implements {@link System#nanoTime} through ShadowWrangler.
   *
   * @return Current time with nanos.
   */
  @SuppressWarnings("unused")
  public static long nanoTime() {
    if (ShadowLooper.looperMode() == LooperMode.Mode.LEGACY) {
      return ShadowLegacySystemClock.nanoTime();
    } else {
      return ShadowPausedSystemClock.uptimeNanos();
    }
  }

  /**
   * Implements {@link System#currentTimeMillis} for framework code through ShadowWrangler. With
   * {@code -Drobolectric.useRealWallClock=true} it is the real wall clock, otherwise the virtual
   * uptime.
   *
   * @deprecated Framework code will read the real wall clock in the next release, which {@code
   *     -Drobolectric.useRealWallClock=true} turns on now. Use {@link System#currentTimeMillis}.
   */
  @Deprecated
  public static long currentTimeMillis() {
    return frameworkCurrentTimeMillis();
  }

  static long frameworkCurrentTimeMillis() {
    if (useRealWallClock()) {
      return System.currentTimeMillis();
    }
    if (ShadowLooper.looperMode() == LooperMode.Mode.LEGACY) {
      return ShadowLegacySystemClock.currentTimeMillis();
    } else {
      return SystemClock.uptimeMillis();
    }
  }

  static boolean useRealWallClock() {
    return Boolean.getBoolean("robolectric.useRealWallClock");
  }
}
