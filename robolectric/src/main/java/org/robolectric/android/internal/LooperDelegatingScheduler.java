package org.robolectric.android.internal;

import static org.robolectric.util.Scheduler.IdleState.PAUSED;

import android.os.SystemClock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.Scheduler;

/**
 * A foreground Scheduler implementation used for {@link LooperMode.Mode#PAUSED}.
 *
 * <p>All API calls will delegate to ShadowLooper.
 */
@SuppressWarnings("UnsynchronizedOverridesSynchronized")
public class LooperDelegatingScheduler extends Scheduler {

  private final ShadowLooper shadowLooper;

  public LooperDelegatingScheduler(ShadowLooper shadowLooper) {
    this.shadowLooper = shadowLooper;
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
    shadowLooper.pause();
  }

  @Override
  public void unPause() {
    shadowLooper.unPause();
  }

  @Override
  public boolean isPaused() {
    return shadowLooper.isPaused();
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
  public boolean advanceToLastPostedRunnable() {
    shadowLooper.runToEndOfTasks();
    return true;
  }

  @Override
  public boolean advanceToNextPostedRunnable() {
    shadowLooper.runToNextTask();
    return true;
  }

  @Override
  public boolean advanceBy(long interval) {
    shadowLooper.idleFor(interval, TimeUnit.MILLISECONDS);
    return true;
  }

  @Override
  public boolean advanceBy(long amount, TimeUnit unit) {
    shadowLooper.idleFor(amount, unit);
    return true;
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public boolean advanceTo(long endTime) {
    shadowLooper.idleFor(Duration.ofMillis(endTime - SystemClock.uptimeMillis()));
    return true;
  }

  @Override
  public boolean runOneTask() {
    shadowLooper.runOneTask();
    return true;
  }

  @Override
  public boolean areAnyRunnable() {
    return !shadowLooper.isIdle();
  }

  @Override
  public void reset() {
    // ignore
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException("size is not supported in PAUSED LooperMode");
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public Duration getNextScheduledTaskTime() {
    return shadowLooper.getNextScheduledTaskTime();
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public Duration getLastScheduledTaskTime() {
    return shadowLooper.getLastScheduledTaskTime();
  }

  @Override
  @Deprecated
  public void idleConstantly(boolean shouldIdleConstantly) {
    throw new UnsupportedOperationException("idleConstantly is not supported in PAUSED LooperMode");
  }
}
