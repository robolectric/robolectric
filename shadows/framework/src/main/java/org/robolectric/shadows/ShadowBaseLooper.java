package org.robolectric.shadows;

import android.os.Looper;
import androidx.test.annotation.Beta;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.LooperMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Scheduler;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

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

  public abstract void quitUnchecked();

  public abstract boolean hasQuit();

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
   * Returns true if the looper has any pending tasks scheduled to be executed before current time.
   */
  public abstract boolean isIdle();

  /**
   * Only supported for {@link LooperMode.Mode.PAUSED}.
   */
  public abstract void unPause();

  public abstract boolean isPaused();

  /**
   * Only supported for {@link LooperMode.Mode.PAUSED}.
   */
  public abstract boolean setPaused(boolean shouldPause);

  /**
   * Only supported for {@link LooperMode.Mode.PAUSED}.
   */
  public abstract void resetScheduler();

  /**
   * Causes all enqueued tasks to be discarded, and pause state to be reset
   */
  public abstract void reset();

  /**
   * Returns the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued tasks.
   * This scheduler is managed by the Looper's associated queue.
   *
   * Only supported for {@link LooperMode.Mode.PAUSED}.
   *
   * @return the {@link org.robolectric.util.Scheduler} that is being used to manage the enqueued tasks.
   */
  public abstract Scheduler getScheduler();

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
   * Causes {@link Runnable}s that have been scheduled to run within the next {@code intervalMillis} milliseconds to
   * run while advancing the scheduler's clock.
   *
   * @deprecated Use {@link #idle(long, TimeUnit)}.
   */
  @Deprecated
  public void idle(long intervalMillis) {
    idleFor(intervalMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Causes {@link Runnable}s that have been scheduled to run within the next specified amount of time to run while
   * advancing the clock.
   *
   * @deprecated use {@link idleFor(amount, unit)}
   */
  @Deprecated
  public void idle(long amount, TimeUnit unit) {
    idleFor(amount, unit);
  }

  public abstract void idleConstantly(boolean shouldIdleConstantly);

  /**
   * Causes all of the {@link Runnable}s that have been scheduled to run while advancing the clock to the
   * start time of the last scheduled {@link Runnable}.
   */
  public void runToEndOfTasks() {
    idleFor(getLastScheduledTaskTime());
  }

  /**
   * Causes the next {@link Runnable}(s) that have been scheduled to run while advancing the clock to its
   * start time. If more than one {@link Runnable} is scheduled to run at this time then they will all be run.
   */
  public void runToNextTask() {
    idleFor(getNextScheduledTaskTime());
  }

  /**
   * Causes only one of the next {@link Runnable}s that have been scheduled to run while advancing the
   * clock to its start time. Only one {@link Runnable} will run even if more than one has ben scheduled to run at the
   * same time.
   */
  public abstract void runOneTask();

  /**
   * Enqueue a task to be run later.
   *
   * @param runnable    the task to be run
   * @param delayMillis how many milliseconds into the (virtual) future to run it
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postDelayed(Runnable,long)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Deprecated
  public abstract boolean post(Runnable runnable, long delayMillis);

  /**
   * Enqueue a task to be run ahead of all other delayed tasks.
   *
   * @param runnable    the task to be run
   * @return true if the runnable is enqueued
   * @see android.os.Handler#postAtFrontOfQueue(Runnable)
   * @deprecated Use a {@link android.os.Handler} instance to post to a looper.
   */
  @Deprecated
  public abstract boolean postAtFrontOfQueue(Runnable runnable);

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
