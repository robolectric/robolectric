package org.robolectric.util;

import static org.robolectric.util.Scheduler.IdleState.CONSTANT_IDLE;
import static org.robolectric.util.Scheduler.IdleState.PAUSED;
import static org.robolectric.util.Scheduler.IdleState.UNPAUSED;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that manages a queue of Runnables that are scheduled to run now (or at some time in
 * the future). Runnables that are scheduled to run on the UI thread (tasks, animations, etc)
 * eventually get routed to a Scheduler instance.
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
 */
public class Scheduler {

  private TaskManager taskManager;

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

  private final static long START_TIME = 100;
  private volatile long currentTime = START_TIME;
  private AtomicBoolean isExecutingRunnable = new AtomicBoolean(false);
  private final Thread associatedThread = Thread.currentThread();
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

  public void register(TaskManager taskManager) {
    this.taskManager = taskManager;
    taskManager.addListener(new TaskManager.Listener() {
      @Override
      public void newTaskPosted() {
        if (isExecutingRunnable.get()) {
          return;
        }

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
    });
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
  public void setIdleState(IdleState idleState) {
    synchronized (idleState) {
      this.idleState = idleState;
    }
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
  public void pause() {
    setIdleState(PAUSED);
  }

  /**
   * Un-pause the scheduler. Equivalent to <tt>setIdleState(UNPAUSED)</tt>.
   *
   * @see #pause()
   * @see #setIdleState(IdleState)
   */
  public void unPause() {
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
  public void post(Runnable runnable) {
    postDelayed(runnable, 0, TimeUnit.MILLISECONDS);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   *
   * @param runnable    Runnable to add.
   * @param delayMillis Delay in millis.
   */
  public void postDelayed(Runnable runnable, long delayMillis) {
    postDelayed(runnable, delayMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   */
  public void postDelayed(Runnable runnable, long delay, TimeUnit unit) {
    long delayMillis = unit.toMillis(delay);
    taskManager.post(runnable, currentTime + delayMillis);
  }

  /**
   * Add a runnable to the head of the queue.
   *
   * @param runnable  Runnable to add.
   */
  public void postAtFrontOfQueue(Runnable runnable) {
    taskManager.postAtFrontOfQueue(runnable);
  }

  /**
   * Remove a runnable from the queue.
   *
   * @param runnable  Runnable to remove.
   */
  public void remove(Runnable runnable) {
    taskManager.remove(runnable);
  }

  /**
   * Run all runnables in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public boolean advanceToLastPostedRunnable() {
    boolean taskRun = false;
    while (true) {
      long scheduledTime = runNextTask();
      if (scheduledTime < 0) {
        break;
      }
      currentTime = scheduledTime;
      taskRun = true;
    }
    return taskRun;
  }

  private long runNextTask() {
    isExecutingRunnable.set(true);
    long time = 0;
    try {
      time = taskManager.runNextTask();
    } finally {
      isExecutingRunnable.set(false);
    }
    return time;
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public boolean advanceToNextPostedRunnable() {
    return runOneTask();
  }

  /**
   * Run all runnables that are scheduled to run in the next time interval.
   *
   * @param   interval  Time interval (in millis).
   * @return  True if a runnable was executed.
   * @deprecated Use {@link #advanceBy(long, TimeUnit)}.
   */
  @Deprecated
  public boolean advanceBy(long interval) {
    return advanceBy(interval, TimeUnit.MILLISECONDS);
  }

  /**
   * Run all runnables that are scheduled to run in the next time interval.
   *
   * @return  True if a runnable was executed.
   */
  public boolean advanceBy(long amount, TimeUnit unit) {
    long endingTime = currentTime + unit.toMillis(amount);
    return advanceTo(endingTime);
  }

  /**
   * Run all runnables that are scheduled before the endTime.
   *
   * @param   endTime   Future time.
   * @return  True if a runnable was executed.
   */
  public boolean advanceTo(long endTime) {
    if (endTime - currentTime < 0 || size() < 1) {
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
  public boolean runOneTask() {
    long scheduledTime = taskManager.getScheduledTimeOfNextTask();
    if (scheduledTime > currentTime) {
      currentTime = scheduledTime;
    }
    return runNextTask() > -1;
  }

  /**
   * Determine if any enqueued runnables are enqueued before the current time.
   *
   * @return  True if any runnables can be executed.
   */
  public boolean areAnyRunnable() {
    return nextTaskIsScheduledBefore(currentTime);
  }

  /**
   * Reset the internal state of the Scheduler.
   */
  public synchronized void reset() {
    taskManager.removeAll();
    idleState = UNPAUSED;
    currentTime = START_TIME;
    isExecutingRunnable.set(false);
  }

  /**
   * Return the number of enqueued runnables.
   *
   * @return  Number of enqueues runnables.
   */
  public int size() {
    return taskManager.size();
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
    return size() > 0 && taskManager.getScheduledTimeOfNextTask() <= endingTime;
  }
}
