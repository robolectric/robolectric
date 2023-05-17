package org.robolectric.shadows;

import android.os.SystemClock;
import java.util.concurrent.TimeUnit;
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
      return TimeUnit.MILLISECONDS.toNanos(SystemClock.uptimeMillis());
    }
  }

  /**
   * Implements {@link System#currentTimeMillis} through ShadowWrangler.
   *
   * @return Current time with millis.
   */
  @SuppressWarnings("unused")
  public static long currentTimeMillis() {
    if (ShadowLooper.looperMode() == LooperMode.Mode.LEGACY) {
      return ShadowLegacySystemClock.currentTimeMillis();
    } else {
      return SystemClock.uptimeMillis();
    }
  }
}
