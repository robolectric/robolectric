package org.robolectric.shadows;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.os.Looper;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.LooperMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/**
 * The base API class for controlling Loopers.
 *
 * It will delegate calls to the appropriate shadow based on the current LooperMode.
 */
public abstract class ShadowBaseLooper {

  /**
   * returns true if realistic looper is enabled
   */
  public static boolean useRealisticLooper() {
    LooperMode.Mode looperMode = ConfigurationRegistry.get(LooperMode.Mode.class);
    return looperMode == LooperMode.Mode.PAUSED;
  }

  /**
   * Executes all posted tasks scheduled before or at the current time.
   */
  public abstract void idle();

  /**
   * Advances the system clock by the given time, then executes all posted tasks scheduled before or
   * at the given time.
   */
  public abstract void idleFor(long time, TimeUnit timeUnit);

  /**
   * A variant of {@link #idleFor(long, TimeUnit)} that accepts a Duration.
   */
  @SuppressWarnings("AndroidJdkLibsChecker")
  public void idleFor(Duration duration) {
    idleFor(duration.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Runs the current task with the looper paused.
   *
   * When LooperMode is PAUSED, this will execute all pending
   */
  public abstract void runPaused(Runnable run);

  /**
   * Helper method to selectively call idle() only if LooperMode is PAUSED.
   *
   * Intended for backwards compatibility, to avoid changing behavior for tests still using LEGACY
   * LooperMode.
   */
  public abstract void idleIfPaused();

  /**
   * Pause the looper.
   *
   * Has no practical effect for realistic looper, since it is always paused.
   */
  public abstract void pause();

  public static ShadowBaseLooper shadowMainLooper() {
    return Shadow.extract(Looper.getMainLooper());
  }

  public static class Picker extends LooperShadowPicker<ShadowBaseLooper> {

    public Picker() {
      super(ShadowLooper.class, ShadowRealisticLooper.class);
    }
  }
}
