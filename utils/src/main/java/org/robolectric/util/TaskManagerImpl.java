package org.robolectric.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;


public class TaskManagerImpl implements TaskManager {

  private final List<ScheduledRunnable> runnables = Collections.synchronizedList(new ArrayList<>());
  private final List<Listener> listeners = new CopyOnWriteArrayList<>();

  /**
   * Add a runnable to the queue to be run at the specified time.
   */
  @Override
  public void post(Runnable runnable, long time) {
    queueRunnableAndSort(runnable, time);
  }

  /**
   * Add a runnable to the head of the queue.
   *
   * @param runnable  Runnable to add.
   */
  @Override
  public void postAtFrontOfQueue(Runnable runnable) {
    runnables.add(0, new ScheduledRunnable(runnable, 0));
    notifyListeners();
  }

  /**
   * Remove a runnable from the queue.
   *
   * @param runnable  Runnable to remove.
   */
  @Override
  public void remove(Runnable runnable) {
    synchronized (runnables) {
      ListIterator<ScheduledRunnable> iterator = runnables.listIterator();
      while (iterator.hasNext()) {
        ScheduledRunnable next = iterator.next();
        if (next.runnable == runnable) {
          iterator.remove();
        }
      }
    }
  }

  /**
   * Run the next runnable in the queue.
   *
   * @return the scheduled time of the executed runnable. -1 if no runnable was executed
   */
  @Override
  public long runNextTask() {
    if (size() < 1) {
      return -1;
    }

    ScheduledRunnable postedRunnable = runnables.remove(0);
    postedRunnable.run();
    return postedRunnable.scheduledTime;
  }

  /**
   * Clear the queue of runnable tasks
   */
  @Override
  public  void removeAll() {
    runnables.clear();
  }

  /**
   * Return the number of enqueued runnables.
   *
   * @return  Number of enqueues runnables.
   */
  @Override
  public int size() {
    return runnables.size();
  }

  @Override
  public long getScheduledTimeOfNextTask() {
    if (size() < 1) {
      return -1;
    }
    return runnables.get(0).scheduledTime;
  }

  @Override
  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  private void queueRunnableAndSort(Runnable runnable, long scheduledTime) {
    synchronized (runnables) {
      runnables.add(new ScheduledRunnable(runnable, scheduledTime));
      Collections.sort(runnables);
    }
    notifyListeners();
  }

  private void notifyListeners() {
    for (Listener listener : listeners) {
      listener.newTaskPosted();
    }
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
      return Long.compare(scheduledTime, runnable.scheduledTime);
    }

    public void run() {
        runnable.run();
    }
  }
}
