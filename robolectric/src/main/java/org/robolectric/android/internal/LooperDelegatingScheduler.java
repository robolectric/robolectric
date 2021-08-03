package org.robolectric.android.internal;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.Scheduler.IdleState.PAUSED;

import android.os.Looper;
import android.os.SystemClock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPausedMessageQueue;
import org.robolectric.util.Scheduler;

/**
 * A foreground Scheduler implementation used for {@link LooperMode.Mode#PAUSED}.
 *
 * <p>All API calls will delegate to ShadowLooper.
 */
@SuppressWarnings("UnsynchronizedOverridesSynchronized")
public class LooperDelegatingScheduler extends Scheduler {

  private final Looper looper;

  public LooperDelegatingScheduler(Looper looper) {
    this.looper = looper;
  }

  @Override
  public IdleState getIdleState() {
    return PAUSED;
  }

  @Override
  public void setIdleState(IdleState idleState) {
    throw new UnsupportedOperationException("setIdleState is not supported in PAUSED LooperMode");
  }

  @Override
  public long getCurrentTime() {
    return SystemClock.uptimeMillis();
  }

  @Override
  public void pause() {
    shadowOf(looper).pause();
  }

  @Override
  public void unPause() {
    shadowOf(looper).unPause();
  }

  @Override
  public boolean isPaused() {
    return shadowOf(looper).isPaused();
  }

  @Override
  public void post(Runnable runnable) {
    // this could be supported, but its a deprecated unnecessary API
    throw new UnsupportedOperationException("post is not supported in PAUSED LooperMode");
  }

  @Override
  public void postDelayed(Runnable runnable, long delayMillis) {
    // this could be supported, but its a deprecated unnecessary API
    throw new UnsupportedOperationException("post is not supported in PAUSED LooperMode");
  }

  @Override
  public void postDelayed(Runnable runnable, long delay, TimeUnit unit) {
    // this could be supported, but its a deprecated unnecessary API
    throw new UnsupportedOperationException("post is not supported in PAUSED LooperMode");
  }

  @Override
  public void postAtFrontOfQueue(Runnable runnable) {
    // this could be supported, but its a deprecated unnecessary API
    throw new UnsupportedOperationException("post is not supported in PAUSED LooperMode");
  }

  @Override
  public void remove(Runnable runnable) {
    // this could be supported, but its a deprecated unnecessary API
    throw new UnsupportedOperationException("remove is not supported in PAUSED LooperMode");
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public boolean advanceToLastPostedRunnable() {
    long scheduledTime = getNextScheduledTaskTime().toMillis();
    shadowOf(looper).runToEndOfTasks();
    return scheduledTime != 0;
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public boolean advanceToNextPostedRunnable() {
    long scheduledTime = getNextScheduledTaskTime().toMillis();
    shadowOf(looper).runToNextTask();
    return scheduledTime != 0;
  }

  @Override
  public boolean advanceBy(long amount, TimeUnit unit) {
    return advanceTo(SystemClock.uptimeMillis() + unit.toMillis(amount));
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public boolean advanceTo(long endTime) {
    if (endTime < SystemClock.uptimeMillis()) {
      return false;
    }
    boolean hasQueueTasks = hasTasksScheduledBefore(endTime);
    shadowOf(looper).idleFor(Duration.ofMillis(endTime - SystemClock.uptimeMillis()));
    return hasQueueTasks;
  }

  @SuppressWarnings("AndroidJdkLibsChecker")
  private boolean hasTasksScheduledBefore(long timeMs) {
    long scheduledTimeMs = getNextScheduledTaskTime().toMillis();
    return scheduledTimeMs > 0 && scheduledTimeMs <= timeMs;
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public boolean runOneTask() {
    long scheduledTime = getNextScheduledTaskTime().toMillis();
    shadowOf(looper).runOneTask();
    return scheduledTime != 0;
  }

  @Override
  public boolean areAnyRunnable() {
    return !shadowOf(looper).isIdle();
  }

  @Override
  public void reset() {
    // ignore
  }

  @Override
  public int size() {
    ShadowPausedMessageQueue shadowQueue = Shadow.extract(looper.getQueue());
    return shadowQueue.internalGetSize();
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public Duration getNextScheduledTaskTime() {
    return shadowOf(looper).getNextScheduledTaskTime();
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public Duration getLastScheduledTaskTime() {
    return shadowOf(looper).getLastScheduledTaskTime();
  }

  @Override
  @Deprecated
  public void idleConstantly(boolean shouldIdleConstantly) {
    throw new UnsupportedOperationException("idleConstantly is not supported in PAUSED LooperMode");
  }
}
