package org.robolectric.util;


public interface TaskManager {

  void post(Runnable runnable, long time);

  void postAtFrontOfQueue(Runnable runnable);

  void remove(Runnable runnable);

  long runNextTask();

  void removeAll();

  int size();

  boolean isEmpty();

  long getScheduledTimeOfNextTask();

  void addListener(Listener listener);

  interface Listener {
    void newTaskPosted();
  }
}
