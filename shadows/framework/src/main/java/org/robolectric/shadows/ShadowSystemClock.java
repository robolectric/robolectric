package org.robolectric.shadows;

import android.os.SystemClock;
import java.time.Duration;
import org.robolectric.annotation.LooperMode;

/**
 * The shadow API for {@link SystemClock}.
 *
 * <p>The behavior of SystemClock in Robolectric will differ based on the current {@link
 * LooperMode}. See {@link ShadowLegacySystemClock} and {@link ShadowPausedSystemClock} for more
 * details.
 */
public abstract class ShadowSystemClock {
  protected static boolean networkTimeAvailable = true;

  /**
   * Sets the value for {@link System#nanoTime()}
   *
   * <p>Can only be used for {@link LooperMode.Mode.LEGACY}. For {@link LooperMode.Mode.PAUSED}, use
   * {@link SystemClock#setCurrentTimeMillis(long)} instead.
   */
  public static void setNanoTime(long nanoTime) {
    ShadowLegacySystemClock.setNanoTime(nanoTime);
  }

  /** Sets whether network time is available. */
  public static void setNetworkTimeAvailable(boolean available) {
    networkTimeAvailable = available;
  }

  /**
   * A convenience method for advancing the clock via {@link SystemClock#setCurrentTimeMillis(long)}
   *
   * @param duration
   */
  public static void advanceBy(Duration duration) {
    SystemClock.setCurrentTimeMillis(SystemClock.uptimeMillis() + duration.toMillis());
  }

  public static void reset() {
    networkTimeAvailable = true;
  }

  static class Picker extends LooperShadowPicker<ShadowSystemClock> {

    public Picker() {
      super(ShadowLegacySystemClock.class, ShadowPausedSystemClock.class);
    }
  }
}
