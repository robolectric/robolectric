package org.robolectric.android.util.concurrent;

/**
 * Utility class for running code off the main looper thread aka Robolectric test thread.
 */
public class BackgroundExecutor {

  private BackgroundExecutor() {}

  // use an inner class reference to lazy load the singleton in a thread-safe manner
  private static class SingletonHolder {
    private static final BackgroundExecutor instance = new BackgroundExecutor();
  }

  private final InlineExecutorService backgroundExecutorService =
      new InlineExecutorService();

  /**
   * A helper method intended for testing production code that needs to run off the main Looper.
   *
   * Will execute given runnable in a background thread and will do a best-effort attempt at
   * propagating any exception back up to caller in their original form.
   */
  public static void runInBackground(Runnable runnable) {
    SingletonHolder.instance.backgroundExecutorService.execute(runnable);
  }
}
