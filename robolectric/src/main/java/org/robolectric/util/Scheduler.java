package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class Scheduler {
  private List<PostedRunnable> postedRunnables = new ArrayList<PostedRunnable>();
  private long currentTime = 0;
  private boolean paused = false;
  private Thread associatedThread = Thread.currentThread();
  private boolean isConstantlyIdling = false;
  private boolean isExecutingRunnable = false;

  public synchronized long getCurrentTime() {
    return currentTime;
  }

  public synchronized void pause() {
    paused = true;
  }

  public synchronized void unPause() {
    paused = false;
    advanceToLastPostedRunnable();
  }

  public synchronized boolean isPaused() {
    return paused;
  }

  public synchronized void postDelayed(Runnable runnable, long delayMillis) {
    if ((!isConstantlyIdling && (paused || delayMillis > 0)) || Thread.currentThread() != associatedThread) {
      queueRunnableAndSort(runnable, currentTime + delayMillis);
    } else {
      runOrQueueRunnable(runnable, currentTime + delayMillis);
    }
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
    postedRunnables.add(new PostedRunnable(runnable, scheduledTime));
    Collections.sort(postedRunnables);
  }

  public synchronized void post(Runnable runnable) {
    postDelayed(runnable, 0);
  }

  public synchronized void postAtFrontOfQueue(Runnable runnable) {
    if (paused || Thread.currentThread() != associatedThread) {
      postedRunnables.add(0, new PostedRunnable(runnable, currentTime));
    } else {
      runOrQueueRunnable(runnable, currentTime);
    }
  }

  public synchronized void remove(Runnable runnable) {
    ListIterator<PostedRunnable> iterator = postedRunnables.listIterator();
    while (iterator.hasNext()) {
      PostedRunnable next = iterator.next();
      if (next.runnable == runnable) {
        iterator.remove();
      }
    }
  }

  public synchronized boolean advanceToLastPostedRunnable() {
    if (enqueuedTaskCount() < 1) {
      return false;
    }

    return advanceTo(postedRunnables.get(postedRunnables.size() - 1).scheduledTime);
  }

  public synchronized boolean advanceToNextPostedRunnable() {
    if (enqueuedTaskCount() < 1) {
      return false;
    }

    return advanceTo(postedRunnables.get(0).scheduledTime);
  }

  public synchronized boolean advanceBy(long intervalMs) {
    long endingTime = currentTime + intervalMs;
    return advanceTo(endingTime);
  }

  public synchronized boolean advanceTo(long endingTime) {
    if (endingTime - currentTime < 0 || enqueuedTaskCount() < 1) {
      currentTime = endingTime;
      return false;
    }

    int runCount = 0;
    while (nextTaskIsScheduledBefore(endingTime)) {
      runOneTask();
      ++runCount;
    }
    currentTime = endingTime;

    return runCount > 0;
  }

  public synchronized boolean runOneTask() {
    if (enqueuedTaskCount() < 1) {
      return false;
    }

    PostedRunnable postedRunnable = postedRunnables.remove(0);
    currentTime = postedRunnable.scheduledTime;
    postedRunnable.run();
    return true;
  }

  public synchronized boolean runTasks(int howMany) {
    if (enqueuedTaskCount() < howMany) {
      return false;
    }

    while (howMany > 0) {
      PostedRunnable postedRunnable = postedRunnables.remove(0);
      currentTime = postedRunnable.scheduledTime;
      postedRunnable.run();
      howMany--;
    }
    return true;
  }

  public synchronized int enqueuedTaskCount() {
    return postedRunnables.size();
  }

  public synchronized boolean areAnyRunnable() {
    return nextTaskIsScheduledBefore(currentTime);
  }

  public synchronized void reset() {
    postedRunnables.clear();
    paused = false;
    isConstantlyIdling = false;
  }

  public synchronized int size() {
    return postedRunnables.size();
  }

  public void idleConstantly(boolean shouldIdleConstantly) {
    isConstantlyIdling = shouldIdleConstantly;
  }

  class PostedRunnable implements Comparable<PostedRunnable> {
    Runnable runnable;
    long scheduledTime;

    PostedRunnable(Runnable runnable, long scheduledTime) {
      this.runnable = runnable;
      this.scheduledTime = scheduledTime;
    }

    @Override
    public int compareTo(PostedRunnable postedRunnable) {
      return (int) (scheduledTime - postedRunnable.scheduledTime);
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

  private boolean nextTaskIsScheduledBefore(long endingTime) {
    return enqueuedTaskCount() > 0 && postedRunnables.get(0).scheduledTime <= endingTime;
  }
}
