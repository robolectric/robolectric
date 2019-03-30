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
    return TimeUnit.MILLISECONDS.toNanos(SystemClock.uptimeMillis());
  }

  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return Current time with millis.
   */
  @SuppressWarnings("unused")
  public static long currentTimeMillis() {
    return SystemClock.uptimeMillis();
  }
}
