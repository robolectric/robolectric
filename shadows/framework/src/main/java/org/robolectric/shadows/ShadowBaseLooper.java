package org.robolectric.shadows;

import android.os.Looper;
import androidx.test.annotation.Beta;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.LooperMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;

/**
 * The base API class for controlling Loopers.
 *
 * It will delegate calls to the appropriate shadow based on the current LooperMode.
 *
 * Beta API, subject to change
 */
@Beta
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
   * When LooperMode is PAUSED, this will execute all pending tasks scheduled before the current
   * time.
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
   * Returns true if looper has no pending tasks which are scheduled for execution at or before
   * current time.
   *
   * Note this does NOT necessarily mean looper is not currently busy executing a task.
   */
  public abstract boolean isIdle();

  /**
   * Pause the looper.
   *
   * Has no practical effect for realistic looper, since it is always paused.
   */
  public abstract void pause();

  /**
   * @return the scheduled time of the next posted task; Duration.ZERO if there is no currently
   * scheduled task.
   */
  public abstract Duration getNextScheduledTaskTime();

  /**
   * @return the scheduled time of the last posted task; Duration.ZERO 0 if there is
   * no currently scheduled task.
   */
  public abstract Duration getLastScheduledTaskTime();

  public static ShadowBaseLooper shadowMainLooper() {
    return Shadow.extract(Looper.getMainLooper());
  }

  public static class Picker extends LooperShadowPicker<ShadowBaseLooper> {

    public Picker() {
      super(ShadowLooper.class, ShadowRealisticLooper.class);
    }
  }
}
