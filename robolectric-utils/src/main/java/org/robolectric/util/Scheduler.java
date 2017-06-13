package org.robolectric.util;

import static org.robolectric.util.Scheduler.IdleState.CONSTANT_IDLE;
import static org.robolectric.util.Scheduler.IdleState.PAUSED;
import static org.robolectric.util.Scheduler.IdleState.UNPAUSED;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

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

  private final Thread associatedThread = Thread.currentThread();
  // guards time when held WITH stateLock
  private final ReentrantLock timeLock = new ReentrantLock();
  // guards everything except time
  private final ReentrantLock stateLock = new ReentrantLock();
  // NOTE: locking order is 1) timeLock, 2) runnables lock. be very careful with any
  // change, as if you are holding a lock but call into another function that attempts to acquire
  // a higher order lock, you risk lock inversion. changing time requires holding both locks at
  // once.
  //
  // thus, holding either lock will guarantee time cannot change, but still allows for enough
  // granularity other state may change

  private final LinkedList<ScheduledRunnable> runnables = new LinkedList<>();
  private volatile boolean isExecutingRunnable = false;
  private volatile long currentTimeMs = START_TIME;
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
  public void setIdleState(IdleState idleState) {
    timeLock.lock();
    try {
      this.idleState = idleState;
      switch (idleState) {
        case UNPAUSED:
          advance(currentTimeMs, false);
          break;
        case CONSTANT_IDLE:
          advance(Long.MAX_VALUE, false);
          break;
        default:
          break;
      }
    } finally {
      timeLock.unlock();
    }
  }

  /**
   * Get the current time (as seen by the scheduler), in milliseconds.
   *
   * @return  Current time in milliseconds.
   */
  public long getCurrentTime() {
    return currentTimeMs;
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
   * @param delayMs Delay in millis.
   * @deprecated Use {@link #postDelayed(Runnable, long, TimeUnit)} instead.
   */
  @Deprecated
  public void postDelayed(Runnable runnable, long delayMs) {
    postDelayed(runnable, delayMs, TimeUnit.MILLISECONDS);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   */
  public void postDelayed(Runnable runnable, long delay, TimeUnit unit) {
    queueAndMaybeRunUnlocked(runnable, unit.toMillis(delay), false);
  }

  /**
   * Add a runnable to the head of the queue.
   *
   * @param runnable  Runnable to add.
   */
  public void postAtFrontOfQueue(Runnable runnable) {
    queueAndMaybeRunUnlocked(runnable, 0, true);
  }

  /**
   * Remove a runnable from the queue.
   *
   * @param runnable  Runnable to remove.
   */
  public void remove(Runnable runnable) {
    stateLock.lock();
    try {
      ListIterator<ScheduledRunnable> iterator = runnables.listIterator();
      while (iterator.hasNext()) {
        ScheduledRunnable next = iterator.next();
        if (next.runnable == runnable) {
          iterator.remove();
        }
      }
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Run all runnables in the queue at the time this method is called.
   *
   * @return  True if a runnable was executed.
   */
  public boolean advanceToLastPostedRunnable() {
    timeLock.lock();
    stateLock.lock();
    try {
      return !runnables.isEmpty() && advanceTo(runnables.peekLast().scheduledTime);
    } finally {
      stateLock.unlock();
      timeLock.unlock();
    }
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public boolean advanceToNextPostedRunnable() {
    timeLock.lock();
    stateLock.lock();
    try {
      return !runnables.isEmpty() && advanceTo(runnables.peek().scheduledTime);
    } finally {
      stateLock.unlock();
      timeLock.unlock();
    }
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
    timeLock.lock();
    try {
      return advanceTo(currentTimeMs + unit.toMillis(amount));
    } finally {
      timeLock.unlock();
    }
  }

  /**
   * Run all runnables that are scheduled before the endTime.
   *
   * @param   endTimeMs   Future time.
   * @return  True if a runnable was executed.
   */
  public boolean advanceTo(long endTimeMs) {
    timeLock.lock();
    try {
      boolean run = false;
      if (endTimeMs >= currentTimeMs) {
        run = advance(endTimeMs, false);
      }
      currentTimeMs = endTimeMs;
      return run;
    } finally {
      timeLock.unlock();
    }
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public boolean runOneTask() {
    return advance(Long.MAX_VALUE, true);
  }

  /**
   * Determine if any enqueued runnables are enqueued before the current time.
   *
   * @return  True if any runnables can be executed.
   */
  public boolean areAnyRunnable() {
    timeLock.lock();
    stateLock.lock();
    try {
      return !runnables.isEmpty() && runnables.peek().scheduledTime <= currentTimeMs;
    } finally {
      stateLock.unlock();
      timeLock.unlock();
    }
  }

  /**
   * Reset the internal state of the Scheduler.
   */
  public void reset() {
    timeLock.lock();
    stateLock.lock();
    try {
      runnables.clear();
      idleState = UNPAUSED;
      currentTimeMs = START_TIME;
      isExecutingRunnable = false;
    } finally {
      stateLock.unlock();
      timeLock.unlock();
    }
  }

  /**
   * Return the number of enqueued runnables.
   *
   * @return  Number of enqueues runnables.
   */
  public int size() {
    stateLock.lock();
    try {
      return runnables.size();
    } finally {
      stateLock.unlock();
    }
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

  // this method must return false if there is any possibility of having to run this runnable. it
  // may optionally return false even when there the scheduler may not have to run the runnables.
  // thus a default simple and correct implementation would simply be to always return false.
  //
  // NOTE: method may be called unlocked, then it may not obey the above contract (but should try)
  // NOTE: method may be called locked, then it must obey the above contract
  private boolean mayQueueRunnableWithoutRunning(long delayMs, boolean front) {
    if (Thread.currentThread() != associatedThread || idleState == PAUSED || isExecutingRunnable) {
      return true;
    }

    return idleState == UNPAUSED && delayMs > 0;
  }

  private void queueAndMaybeRunUnlocked(Runnable runnable, long delayMs, boolean front) {
    // NOTE: useful during development to fail tests early rather than trying to debug deadlock
    //if (stateLock.isHeldByCurrentThread()) {
    //  throw new AssertionError("cannot hold stateLock while calling this method");
    //}

    // double checked locking and a spin lock, how fun and pretty
    while (true) {
      if (mayQueueRunnableWithoutRunning(delayMs, front)) {
        stateLock.lock();
        try {
          if (mayQueueRunnableWithoutRunning(delayMs, front)) {
            queueRunnableLocked(runnable, delayMs, front);
            return;
          }
        } finally {
          stateLock.unlock();
        }
      } else {
        timeLock.lock();
        stateLock.lock();
        try {
          if (!mayQueueRunnableWithoutRunning(delayMs, front)) {
            queueRunnableLocked(runnable, delayMs, front);

            switch (idleState) {
              case CONSTANT_IDLE:
                advance(Long.MAX_VALUE, false);
                break;
              case UNPAUSED:
                advance(currentTimeMs, false);
                break;
              default:
            }
            return;
          }
        } finally {
          stateLock.unlock();
          timeLock.unlock();
        }
      }

      Thread.yield();
    }
  }

  // stateLock must be held
  private void queueRunnableLocked(Runnable runnable, long delayMs, boolean front) {
    // NOTE: useful during development to fail tests early rather than trying to debug deadlock
    //if (!stateLock.isHeldByCurrentThread()) {
    //  throw new AssertionError("must hold stateLock while calling this method");
    //}

    long scheduledTimeMs = currentTimeMs + delayMs;
    ScheduledRunnable scheduled = new ScheduledRunnable(runnable, scheduledTimeMs);

    ListIterator<ScheduledRunnable> scheduledIterator = runnables.listIterator();
    while (scheduledIterator.hasNext()) {
      if (front) {
        if (scheduledTimeMs <= scheduledIterator.next().scheduledTime) {
          scheduledIterator.previous();
          break;
        }
      } else {
        if (scheduledTimeMs < scheduledIterator.next().scheduledTime) {
          scheduledIterator.previous();
          break;
        }
      }
    }

    scheduledIterator.add(scheduled);
  }

  // ideally time would only be advanced from the associated thread of this scheduler, but that
  // would require breaking changes.
  // NOTE: the strictOnlyOneRunnable only exists to match legacy behavior EXACTLY
  private boolean advance(long maxTime, boolean strictOnlyOneRunnable) {
    int numRunnablesExecuted = 0;

    timeLock.lock();
    stateLock.lock();
    try {
      if (isExecutingRunnable) {
        return false;
      }

      do {
        ListIterator<ScheduledRunnable> scheduledIterator = runnables.listIterator();
        if (!scheduledIterator.hasNext()) {
          break;
        }

        ScheduledRunnable task = scheduledIterator.next();
        if (task.scheduledTime > maxTime) {
          break;
        }

        scheduledIterator.remove();

        currentTimeMs = Math.max(currentTimeMs, task.scheduledTime);

        isExecutingRunnable = true;
        final int lockCount = stateLock.getHoldCount();
        for (int i = 0; i < lockCount; ++i) {
          stateLock.unlock();
        }
        try {
          task.runnable.run();
          ++numRunnablesExecuted;
        } finally {
          for (int i = 0; i < lockCount; ++i) {
            stateLock.lock();
          }
          isExecutingRunnable = false;
        }
      } while (!strictOnlyOneRunnable);

      // the executed tasks may have changed the idle state
      if (numRunnablesExecuted > 0 && !strictOnlyOneRunnable) {
        switch (idleState) {
          case UNPAUSED:
            advance(currentTimeMs, false);
            break;
          case CONSTANT_IDLE:
            advance(Long.MAX_VALUE, false);
            break;
          default:
            break;
        }
      }

      return numRunnablesExecuted > 0;
    } finally {
      stateLock.unlock();
      timeLock.unlock();
    }
  }

  private static class ScheduledRunnable implements Comparable<ScheduledRunnable> {

    private final Runnable runnable;
    private final long scheduledTime;

    private ScheduledRunnable(Runnable runnable, long scheduledTime) {
      this.runnable = runnable;
      this.scheduledTime = scheduledTime;
    }

    @Override
    public int compareTo(ScheduledRunnable runnable) {
      return Long.compare(scheduledTime, runnable.scheduledTime);
    }
  }
}
