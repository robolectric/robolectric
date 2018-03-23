package org.robolectric.util;


interface TaskManager {

  void post(Runnable runnable, long time);

  void postAtFrontOfQueue(Runnable runnable);

  void remove(Runnable runnable);

  long runNextTask();

  void reset();

  int size();

  long getScheduledTimeOfNextTask();
}
