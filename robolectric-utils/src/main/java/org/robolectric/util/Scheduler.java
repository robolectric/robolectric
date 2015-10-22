package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;
import static org.robolectric.util.Scheduler.IdleState.*;

/**
 * Class that manages a queue of Runnables that are scheduled to run now (or at some time in
 * the future). Runnables that are scheduled to run on the UI thread (tasks, animations, etc)
 * eventually get routed to a Scheduler instance. If org.robolectric.RoboSettings#isUseGlobalScheduler()
 * is <tt>true</tt>, then there will only be one instance of this class which is used by all components
 * in the test.
 * 
 * The execution of a scheduler can be in one of three states:
 * <ul><li>paused ({@link #pause()}): if paused, then no posted events will be run unless the Scheduler
 * is explicitly instructed to do so.</li>
 * <li>normal ({@link #unPause()}): if not paused but not set to idle constantly, then the Scheduler will
 * automatically run any {@link Runnable}s that are scheduled to run at or before the
 * Scheduler's current time, but it won't automatically run any future events. To
 * run future events the Scheduler needs to have its clock advanced.</li>
 * <li>idling constantly: if {@link #idleConstantly(boolean)} is called with
 * <tt>true</tt>, then the Scheduler will continue looping through posted events
 * (including future events), advancing its clock as it goes.</li>
 * </ul>
 *
 * The scheduler has nanosecond precision internally, but because the most common post operations
 * are done in milliseconds (and for backwards compatibility) all of the API methods assume
 * milliseconds if the required precision is not specified.
 */
public class Scheduler {

  /**
   * Describes the current state of a {@link Scheduler}.
   */
  public static enum IdleState {
    /**
     * The <tt>Scheduler</tt> will not automatically advance the clock nor execute any runnables.
     */
    PAUSED,
    /**
     * The <tt>Scheduler</tt>'s clock won't automatically advance the clock but will automatically
     * execute any runnables scheduled to execute at or before the current time.
     */
    UNPAUSED,
    /**
     * The <tt>Scheduler</tt> will automatically execute any runnables (past, present or future)
     * as soon as they are posted and advance the clock if necessary.
     */
    CONSTANT_IDLE
  }

  /** Time for this scheduler, measured in nanoseconds. */
  private long currentTime = 100000000;
  private boolean isExecutingRunnable = false;
  private final Thread associatedThread = Thread.currentThread();
  private final List<ScheduledRunnable> runnables = new ArrayList<>();
  private IdleState idleState = UNPAUSED;

  /**
   * Retrieves the current idling state of this <tt>Scheduler</tt>.
   * @return The current idle state of this <tt>Scheduler</tt>.
   * @see #setIdleState(IdleState)
   * @see #isPaused()
   */
  public synchronized IdleState getIdleState() {
    return idleState;
  }

  /**
   * Sets the current idling state of this <tt>Scheduler</tt>. If transitioning to the
   * {@link IdleState#UNPAUSED} state any tasks scheduled to be run at or before the current time
   * will be run, and if transitioning to the {@link IdleState#CONSTANT_IDLE} state all scheduled
   * tasks will be run and the clock advanced to the time of the last runnable.
   * @param idleState The new idle state of this <tt>Scheduler</tt>.
   * @see #setIdleState(IdleState)
   * @see #isPaused()
   */
  public synchronized void setIdleState(IdleState idleState) {
    this.idleState = idleState;
    switch (idleState) {
      case UNPAUSED:
        advanceBy(0);
        break;
      case CONSTANT_IDLE:
        advanceToLastPostedRunnable();
        break;
    }
  }

  /**
   * Get the current time (as seen by the scheduler), in milliseconds. Equivalent to
   * {@link #getCurrentTime(TimeUnit) getCurrentTime(MILLISECONDS}}.
   *
   * @return  Current time of this scheduler in milliseconds.
   * @see #getCurrentTime(TimeUnit)
   */
  public synchronized long getCurrentTime() {
    return getCurrentTime(MILLISECONDS);
  }

  /**
   * Get the current time (as seen by the scheduler), in the specified units.
   *
   * @param units the time units in which to return the current time.
   * @return  Current time in the given time units.
   * @see #getCurrentTime()
   */
  public synchronized long getCurrentTime(TimeUnit units) {
    return units.convert(currentTime, NANOSECONDS);
  }

  /**
   * Pause the scheduler. Equivalent to <tt>setIdleState(PAUSED)</tt>.
   *
   * @see #unPause()
   * @see #setIdleState(IdleState)
   */
  public synchronized void pause() {
    setIdleState(PAUSED);
  }

  /**
   * Un-pause the scheduler. Equivalent to <tt>setIdleState(UNPAUSED)</tt>.
   *
   * @see #pause()
   * @see #setIdleState(IdleState)
   */
  public synchronized void unPause() {
    setIdleState(UNPAUSED);
  }

  /**
   * Determine if the scheduler is paused.
   *
   * @return  <tt>true</tt> if it is paused.
   */
  public synchronized boolean isPaused() {
    return idleState == PAUSED;
  }

  /**
   * Add a runnable to the queue.
   *
   * @param runnable    Runnable to add.
   */
  public synchronized void post(Runnable runnable) {
    postDelayed(runnable, 0);
  }

  /**
   * Add a runnable to the queue to be run after a delay. Equivalent to
   * {@link #postDelayed(Runnable, long, TimeUnit) postDelayed(runnable,delayMillis,MILLISECONDS}}.
   *
   * @param runnable    the {@link Runnable} to add to the queue.
   * @param delayMillis delay in milliseconds.
   * @see #postDelayed(Runnable, long, TimeUnit)
   */
  public synchronized void postDelayed(Runnable runnable, long delayMillis) {
    postDelayed(runnable, delayMillis, MILLISECONDS);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   *
   * @param runnable Runnable to add.
   * @param delay    delay (in the specified time units).
   * @param units    the time units used to measure the delay.
   */
  public synchronized void postDelayed(Runnable runnable, long delay, TimeUnit units) {
    final long postTimeNanos = currentTime + units.toNanos(delay);
    if ((idleState != CONSTANT_IDLE && (isPaused() || delay > 0)) || Thread.currentThread() != associatedThread) {
      queueRunnableAndSort(runnable, postTimeNanos);
    } else {
      runOrQueueRunnable(runnable, postTimeNanos);
    }
  }

  /**
   * Add a runnable to the head of the queue.
   *
   * @param runnable  Runnable to add.
   */
  public synchronized void postAtFrontOfQueue(Runnable runnable) {
    if (isPaused() || Thread.currentThread() != associatedThread) {
      runnables.add(0, new ScheduledRunnable(runnable, currentTime));
    } else {
      runOrQueueRunnable(runnable, currentTime);
    }
  }

  /**
   * Remove a runnable from the queue.
   *
   * @param runnable  Runnable to remove.
   */
  public synchronized void remove(Runnable runnable) {
    ListIterator<ScheduledRunnable> iterator = runnables.listIterator();
    while (iterator.hasNext()) {
      ScheduledRunnable next = iterator.next();
      if (next.runnable == runnable) {
        iterator.remove();
      }
    }
  }

  /**
   * Run all runnables in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceToLastPostedRunnable() {
    return size() >= 1 && advanceTo(runnables.get(runnables.size() - 1).scheduledTime, NANOSECONDS);
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceToNextPostedRunnable() {
    return size() >= 1 && advanceTo(runnables.get(0).scheduledTime, NANOSECONDS);
  }

  /**
   * Run all runnables that are scheduled to run in the next time interval. The clock is advanced
   * by the specified amount. Equivalent to
   * {@link #advanceBy(long, TimeUnit) advanceBy(interval, MILLISECONDS)}.
   *
   * @param   interval  Time interval (in millis).
   * @return  True if a runnable was executed while the clock was being advanced.
   * @see #advanceBy(long, TimeUnit)
   * @see #advanceTo(long)
   */
  public synchronized boolean advanceBy(long interval) {
    return advanceBy(interval, MILLISECONDS);
  }

  /**
   * Run all runnables that are scheduled to run in the next time interval. The clock is advanced
   * by the specified amount.
   *
   * @param   interval  time interval (in the given units).
   * @param   units the time units in which the interval is specified.
   * @return  <tt>true</tt> if a runnable was executed.
   * @see #advanceBy(long)
   * @see #advanceTo(long, TimeUnit)
   */
  public synchronized boolean advanceBy(long interval, TimeUnit units) {
    return advanceTo(currentTime + units.toNanos(interval), NANOSECONDS);
  }

  /**
   * Run all runnables that are scheduled before the endTime. Equivalent to
   * {@link #advanceTo(long, TimeUnit) advanceTo(endTime, MILLISECONDS)}.
   *
   * @param   endTime  future time to advance to, in milliseconds.
   * @return  <tt>true</tt> if a runnable was executed.
   * @see #advanceTo(long, TimeUnit)
   * @see #advanceBy(long)
   */
  public synchronized boolean advanceTo(long endTime) {
    return advanceTo(endTime, MILLISECONDS);
  }

  /**
   * Run all runnables that are scheduled before the endTime.
   *
   * @param   endTime future time to advance to.
   * @param   units   units in which <tt>endTime</tt> is measured.
   * @return  <tt>true</tt> if a runnable was executed.
   * @see #advanceTo(long)
   * @see #advanceBy(long, TimeUnit)
   */
  public synchronized boolean advanceTo(long endTime, TimeUnit units) {
    final long endTimeNanos = units.toNanos(endTime);
    if (endTimeNanos - currentTime < 0 || size() < 1) {
      currentTime = endTimeNanos;
      return false;
    }

    int runCount = 0;
    while (nextTaskIsScheduledBefore(endTimeNanos)) {
      runOneTask();
      ++runCount;
    }
    currentTime = endTimeNanos;
    return runCount > 0;
  }

  /**
   * Run the next runnable in the queue, advancing the clock if necessary.
   *
   * @return  True if a runnable was executed.
   */
  public synchronized boolean runOneTask() {
    if (size() < 1) {
      return false;
    }

    ScheduledRunnable postedRunnable = runnables.remove(0);
    currentTime = postedRunnable.scheduledTime;
    postedRunnable.run();
    return true;
  }

  /**
   * Determine if any enqueued runnables are enqueued before the current time.
   *
   * @return  <tt>true</tt> if any runnables can be executed.
   */
  public synchronized boolean areAnyRunnable() {
    return nextTaskIsScheduledBefore(currentTime);
  }

  /**
   * Reset the internal state of the Scheduler. Clears the runnable queue and sets the
   * <tt>idleState</tt> back to {@link IdleState#UNPAUSED UNPAUSED}.
   */
  public synchronized void reset() {
    runnables.clear();
    idleState = UNPAUSED;
  }

  /**
   * Return the number of enqueued runnables.
   *
   * @return  Number of enqueues runnables.
   */
  public synchronized int size() {
    return runnables.size();
  }

  /**
   * Set the idle state of the Scheduler. If necessary, the clock will be advanced and runnables
   * executed as required by the newly-set state.
   *
   * @param shouldIdleConstantly  If <tt>true</tt> the idle state will be set to
   *                              {@link IdleState#CONSTANT_IDLE}, otherwise it will be set to
   *                              {@link IdleState#UNPAUSED}.
   * @deprecated This method is ambiguous in how it should behave when turning off constant idle.
   * Use {@link #setIdleState(IdleState)} instead to explicitly set the state.
   */
  @Deprecated
  public void idleConstantly(boolean shouldIdleConstantly) {
    setIdleState(shouldIdleConstantly ? CONSTANT_IDLE : UNPAUSED);
  }

  private boolean nextTaskIsScheduledBefore(long endingTime) {
    return size() > 0 && runnables.get(0).scheduledTime <= endingTime;
  }

  private void runOrQueueRunnable(Runnable runnable, long scheduledTime) {
    if (isExecutingRunnable) {
      queueRunnableAndSort(runnable, scheduledTime);
      return;
    }
    isExecutingRunnable = true;
    try {
      runnable.run();
    } finally {
      isExecutingRunnable = false;
    }
    if (scheduledTime > currentTime) {
      currentTime = scheduledTime;
    }
    // The runnable we just ran may have queued other runnables. If there are
    // any pending immediate execution we should run these now too, unless we are
    // paused.
    switch (idleState) {
      case CONSTANT_IDLE:
        advanceToLastPostedRunnable();
        break;
      case UNPAUSED:
        advanceBy(0);
        break;
    }
  }

  private void queueRunnableAndSort(Runnable runnable, long scheduledTime) {
    runnables.add(new ScheduledRunnable(runnable, scheduledTime));
    Collections.sort(runnables);
  }

  private class ScheduledRunnable implements Comparable<ScheduledRunnable>, Runnable {
    private final Runnable runnable;
    /** Scheduled time in nanoseconds. */
    private final long scheduledTime;

    private ScheduledRunnable(Runnable runnable, long scheduledTime) {
      this.runnable = runnable;
      this.scheduledTime = scheduledTime;
    }

    @Override
    public int compareTo(ScheduledRunnable runnable) {
      return Long.compare(scheduledTime, runnable.scheduledTime);
    }

    @Override
    public void run() {
      isExecutingRunnable = true;
      try {
        runnable.run();
      } finally {
        isExecutingRunnable = false;
      }
    }

    @Override
    public String toString() {
      return "[" + scheduledTime + "]: " + runnable;
    }
  }
}
