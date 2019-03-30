package org.robolectric.shadows;

import android.os.SystemClock;
import java.util.concurrent.TimeUnit;

public class ShadowSystem {

  /**
   * Implements {@link System#nanoTime} through ShadowWrangler.
   *
   * @return Current time with nanos.
   */
  @SuppressWarnings("unused")
  public static long nanoTime() {
    if (ShadowBaseLooper.useRealisticLooper()) {
      return TimeUnit.MILLISECONDS.toNanos(SystemClock.uptimeMillis());
    } else {
      return ShadowSystemClock.nanoTime();
    }
  }

  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return Current time with millis.
   */
  @SuppressWarnings("unused")
  public static long currentTimeMillis() {
    if (ShadowBaseLooper.useRealisticLooper()) {
      return SystemClock.uptimeMillis();
    } else {
      return ShadowSystemClock.currentTimeMillis();
    }
  }
}
