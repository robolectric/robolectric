package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Class that manages a queue of Runnables that are scheduled to run now (or at some time in
 * the future). Runnables that are scheduled to run on the UI thread (tasks, animations, etc)
 * eventually get routed to a Scheduler instance.
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
   * @return  True if it is paused.
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
    postDelayed(runnable, 0);
  }

  /**
   * Add a runnable to the queue to be run after a delay.
   *
   * @param runnable    Runnable to add.
   * @param delayMillis Delay in millis.
   */
  public synchronized void postDelayed(Runnable runnable, long delayMillis) {
    if ((!isConstantlyIdling && (paused || delayMillis > 0)) || Thread.currentThread() != associatedThread) {
      queueRunnableAndSort(runnable, currentTime + delayMillis);
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
    if (paused || Thread.currentThread() != associatedThread) {
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
  }

  private void queueRunnableAndSort(Runnable runnable, long scheduledTime) {
    runnables.add(new ScheduledRunnable(runnable, scheduledTime));
    Collections.sort(runnables);
  }

  private class ScheduledRunnable implements Comparable<ScheduledRunnable> {
    private final Runnable runnable;
    private final long scheduledTime;

    private ScheduledRunnable(Runnable runnable, long scheduledTime) {
      this.runnable = runnable;
      this.scheduledTime = scheduledTime;
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
