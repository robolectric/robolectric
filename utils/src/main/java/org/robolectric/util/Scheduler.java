package org.robolectric.util;

import static org.robolectric.util.Scheduler.IdleState.CONSTANT_IDLE;
import static org.robolectric.util.Scheduler.IdleState.PAUSED;
import static org.robolectric.util.Scheduler.IdleState.UNPAUSED;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

/**
 * Class that manages a queue of Runnables that are scheduled to run now (or at some time in the
 * future). Runnables that are scheduled to run on the UI thread (tasks, animations, etc) eventually
 * get routed to a Scheduler instance.
 *
 * <p>The execution of a scheduler can be in one of three states:
 *
 * <ul>
 *   <li>paused ({@link #pause()}): if paused, then no posted events will be run unless the
 *       Scheduler is explicitly instructed to do so, correctly matching Android's behavior.
 *   <li>normal ({@link #unPause()}): if not paused but not set to idle constantly, then the
 *       Scheduler will automatically run any {@link Runnable}s that are scheduled to run at or
 *       before the Scheduler's current time, but it won't automatically run any future events. To
 *       run future events the Scheduler needs to have its clock advanced.
 *   <li>idling constantly: if {@link #idleConstantly(boolean)} is called with <tt>true</tt>, then
 *       the Scheduler will continue looping through posted events (including future events),
 *       advancing its clock as it goes.
 * </ul>
 */
public class Scheduler {

  /**
   * Describes the current state of a {@link Scheduler}.
   */
  public enum IdleState {
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

  private static final long START_TIME = 100;
  private volatile long currentTime = START_TIME;
  /**
   * PriorityQueue doesn't maintain ordering based on insertion; track that ourselves to preserve
   * FIFO order for posted runnables with the same scheduled time.
   */
  private long nextTimeDisambiguator = 0;

  private boolean isExecutingRunnable = false;
  private final Thread associatedThread = Thread.currentThread();
  private final PriorityQueue<ScheduledRunnable> runnables = new PriorityQueue<>();
  private volatile IdleState idleState = UNPAUSED;

  /**
   * Retrieves the current idling state of this <tt>Scheduler</tt>.
   * @return The current idle state of this <tt>Scheduler</tt>.
   * @see #setIdleState(IdleState)
   * @see #isPaused()
   */
  public IdleState getIdleState() {
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
      default:
    }
  }

  /**
   * Get the current time (as seen by the scheduler), in milliseconds.
   *
   * @return  Current time in milliseconds.
   */
  public long getCurrentTime() {
    return currentTime;
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
  public boolean isPaused() {
    return idleState == PAUSED;
  }

  /**
   * Add a runnable to the queue.
   *
   * @param runnable    Runnable to add.
   */
  public synchronized void post(Runnable runnable) {
    postDelayed(runnable, 0, TimeUnit.MILLISECONDS);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   *
   * @param runnable    Runnable to add.
   * @param delayMillis Delay in millis.
   */
  public synchronized void postDelayed(Runnable runnable, long delayMillis) {
    postDelayed(runnable, delayMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   */
  public synchronized void postDelayed(Runnable runnable, long delay, TimeUnit unit) {
    long delayMillis = unit.toMillis(delay);
    if ((idleState != CONSTANT_IDLE && (isPaused() || delayMillis > 0)) || Thread.currentThread() != associatedThread) {
      runnables.add(new ScheduledRunnable(runnable, currentTime + delayMillis));
    } else {
      runOrQueueRunnable(runnable, currentTime + delayMillis);
    }
  }

  /**
   * Add a runnable to the head of the queue.
   *
   * @param runnable  Runnable to add.
   */
  public synchronized void postAtFrontOfQueue(Runnable runnable) {
    if (isPaused() || Thread.currentThread() != associatedThread) {
      final long timeDisambiguator;
      if (runnables.isEmpty()) {
        timeDisambiguator = nextTimeDisambiguator++;
      } else {
        timeDisambiguator = runnables.peek().timeDisambiguator - 1;
      }
      runnables.add(new ScheduledRunnable(runnable, 0, timeDisambiguator));
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
    Iterator<ScheduledRunnable> iterator = runnables.iterator();
    while (iterator.hasNext()) {
      if (iterator.next().runnable == runnable) {
        iterator.remove();
      }
    }
  }

  /**
   * Run all runnables in the queue, and any additional runnables they schedule that are scheduled
   * before the latest scheduled runnable currently in the queue.
   *
   * @return True if a runnable was executed.
   */
  public synchronized boolean advanceToLastPostedRunnable() {
    long currentMaxTime = currentTime;
    for (ScheduledRunnable scheduled : runnables) {
      if (currentMaxTime < scheduled.scheduledTime) {
        currentMaxTime = scheduled.scheduledTime;
      }
    }
    return advanceTo(currentMaxTime);
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceToNextPostedRunnable() {
    return !runnables.isEmpty() && advanceTo(runnables.peek().scheduledTime);
  }

  /**
   * Run all runnables that are scheduled to run in the next time interval.
   *
   * @param   interval  Time interval (in millis).
   * @return  True if a runnable was executed.
   * @deprecated Use {@link #advanceBy(long, TimeUnit)}.
   */
  @Deprecated
  public synchronized boolean advanceBy(long interval) {
    return advanceBy(interval, TimeUnit.MILLISECONDS);
  }

  /**
   * Run all runnables that are scheduled to run in the next time interval.
   *
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceBy(long amount, TimeUnit unit) {
    long endingTime = currentTime + unit.toMillis(amount);
    return advanceTo(endingTime);
  }

  /**
   * Run all runnables that are scheduled before the endTime.
   *
   * @param   endTime   Future time.
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceTo(long endTime) {
    if (endTime < currentTime || runnables.isEmpty()) {
      currentTime = endTime;
      return false;
    }

    int runCount = 0;
    while (nextTaskIsScheduledBefore(endTime)) {
      runOneTask();
      ++runCount;
    }
    currentTime = endTime;
    return runCount > 0;
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public synchronized boolean runOneTask() {
    ScheduledRunnable postedRunnable = runnables.poll();
    if (postedRunnable != null) {
      if (postedRunnable.scheduledTime > currentTime) {
        currentTime = postedRunnable.scheduledTime;
      }
      postedRunnable.run();
      return true;
    }
    return false;
  }

  /**
   * Determine if any enqueued runnables are enqueued before the current time.
   *
   * @return  True if any runnables can be executed.
   */
  public synchronized boolean areAnyRunnable() {
    return nextTaskIsScheduledBefore(currentTime);
  }

  /**
   * Reset the internal state of the Scheduler.
   */
  public synchronized void reset() {
    runnables.clear();
    idleState = UNPAUSED;
    currentTime = START_TIME;
    isExecutingRunnable = false;
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
    return !runnables.isEmpty() && runnables.peek().scheduledTime <= endingTime;
  }

  private void runOrQueueRunnable(Runnable runnable, long scheduledTime) {
    if (isExecutingRunnable) {
      runnables.add(new ScheduledRunnable(runnable, scheduledTime));
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
      default:
    }
  }

  private class ScheduledRunnable implements Comparable<ScheduledRunnable> {
    private final Runnable runnable;
    private final long scheduledTime;
    private final long timeDisambiguator;

    private ScheduledRunnable(Runnable runnable, long scheduledTime) {
      this(runnable, scheduledTime, nextTimeDisambiguator++);
    }

    private ScheduledRunnable(Runnable runnable, long scheduledTime, long timeDisambiguator) {
      this.runnable = runnable;
      this.scheduledTime = scheduledTime;
      this.timeDisambiguator = timeDisambiguator;
    }

    @Override
    public int compareTo(ScheduledRunnable runnable) {
      int timeCompare = Long.compare(scheduledTime, runnable.scheduledTime);
      if (timeCompare == 0) {
        return Long.compare(timeDisambiguator, runnable.timeDisambiguator);
      }
      return timeCompare;
    }

    public void run() {
      isExecutingRunnable = true;
      try {
        runnable.run();
      } finally {
        isExecutingRunnable = false;
      }
    }
  }
}
