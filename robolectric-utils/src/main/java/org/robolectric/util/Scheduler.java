package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

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
  private long currentTime = 0;
  private boolean paused = false;
  private boolean isConstantlyIdling = false;
  private boolean isExecutingRunnable = false;
  private final Thread associatedThread = Thread.currentThread();
  private final List<ScheduledRunnable> runnables = new ArrayList<>();

  /**
   * Get the current time (as seen by the scheduler).
   *
   * @return  Current time.
   */
  public synchronized long getCurrentTime() {
    return currentTime;
  }

  /**
   * Pause the scheduler.
   */
  public synchronized void pause() {
    paused = true;
  }

  /**
   * Un-pause the scheduler.
   */
  public synchronized void unPause() {
    paused = false;
    advanceToLastPostedRunnable();
  }

  /**
   * Determine if the scheduler is paused.
   *
   * @return  <tt>true</tt> if it is paused.
   */
  public synchronized boolean isPaused() {
    return paused;
  }

  /**
   * Add a runnable to the queue.
   *
   * @param runnable    Runnable to add.
   */
  public synchronized void post(Runnable runnable) {
    post(runnable, null);
  }

  /**
   * Add a runnable to the queue.
   *
   * @param runnable    Runnable to add.
   * @param token       Token for runnable.
   */
  public synchronized void post(Runnable runnable, Object token) {
    postDelayed(runnable, 0, token);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   *
   * @param runnable    Runnable to add.
   * @param delayMillis Delay in millis.
   */
  public synchronized void postDelayed(Runnable runnable, long delayMillis) {
    postDelayed(runnable, delayMillis, null);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   *
   * @param runnable    Runnable to add.
   * @param delayMillis Delay in millis.
   * @param token       Token for runnable.
   */
  public synchronized void postDelayed(Runnable runnable, long delayMillis, Object token) {
    if ((!isConstantlyIdling && (paused || delayMillis > 0)) || Thread.currentThread() != associatedThread) {
      queueRunnableAndSort(runnable, currentTime + delayMillis, token);
    } else {
      runOrQueueRunnable(runnable, currentTime + delayMillis, token);
    }
  }

  /**
   * Add a runnable to the head of the queue.
   *
   * @param runnable  Runnable to add.
   */
  public synchronized void postAtFrontOfQueue(Runnable runnable) {
    postAtFrontOfQueue(runnable, null);
  }

  /**
   * Add a runnable to the head of the queue.
   *
   * @param runnable  Runnable to add.
   * @param token       Token for runnable.
   */
  public synchronized void postAtFrontOfQueue(Runnable runnable, Object token) {
    if (paused || Thread.currentThread() != associatedThread) {
      runnables.add(0, new ScheduledRunnable(runnable, currentTime));
    } else {
      runOrQueueRunnable(runnable, currentTime, token);
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
   * Remove a runnable from the queue with the specified token.
   *
   * @param token Token to remove.
   */
  public synchronized void removeWithToken(Object token) {
    if (token == null) throw new NullPointerException("Token must not be null");
    ListIterator<ScheduledRunnable> iterator = runnables.listIterator();
    while (iterator.hasNext()) {
      ScheduledRunnable next = iterator.next();
      if (next.token == token) {
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
    return size() >= 1 && advanceTo(runnables.get(runnables.size() - 1).scheduledTime);
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceToNextPostedRunnable() {
    return size() >= 1 && advanceTo(runnables.get(0).scheduledTime);
  }

  /**
   * Run all runnables that are scheduled to run in the next time interval.
   *
   * @param   interval  Time interval (in millis).
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceBy(long interval) {
    long endingTime = currentTime + interval;
    return advanceTo(endingTime);
  }

  /**
   * Run all runnables that are scheduled before the endTime.
   *
   * @param   endTime   Future time.
   * @return  True if a runnable was executed.
   */
  public synchronized boolean advanceTo(long endTime) {
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
    paused = false;
    isConstantlyIdling = false;
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
   * Set the idle state of the Scheduler.
   *
   * @param shouldIdleConstantly  True if the scheduler should idle.
   */
  public void idleConstantly(boolean shouldIdleConstantly) {
    isConstantlyIdling = shouldIdleConstantly;
  }

  private boolean nextTaskIsScheduledBefore(long endingTime) {
    return size() > 0 && runnables.get(0).scheduledTime <= endingTime;
  }

  private void runOrQueueRunnable(Runnable runnable, long scheduledTime, Object token) {
    if (isExecutingRunnable) {
      queueRunnableAndSort(runnable, scheduledTime, token);
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
    if (isConstantlyIdling) {
      advanceToLastPostedRunnable();
    } else if (!paused) {
      advanceBy(0);
    }
  }

  private void queueRunnableAndSort(Runnable runnable, long scheduledTime, Object token) {
    runnables.add(new ScheduledRunnable(runnable, scheduledTime, token));
    Collections.sort(runnables);
  }

  private class ScheduledRunnable implements Comparable<ScheduledRunnable> {
    private final Runnable runnable;
    private final long scheduledTime;
    private final Object token;

    private ScheduledRunnable(Runnable runnable, long scheduledTime) {
      this.runnable = runnable;
      this.scheduledTime = scheduledTime;
      token = null;
    }

    private ScheduledRunnable(Runnable runnable, long scheduledTime, Object token) {
      this.runnable = runnable;
      this.scheduledTime = scheduledTime;
      this.token = token;
    }

    @Override
    public int compareTo(ScheduledRunnable runnable) {
      return (int) (scheduledTime - runnable.scheduledTime);
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
