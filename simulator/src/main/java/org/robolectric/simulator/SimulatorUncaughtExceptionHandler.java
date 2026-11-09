package org.robolectric.simulator;

import android.util.Log;

/**
 * The default uncaught exception handler of the simulator. Like the one Android's {@code
 * RuntimeInit} installs in every app process, it logs the crash as a "FATAL EXCEPTION" and then
 * ends the process.
 */
final class SimulatorUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

  private final Runnable exit;

  private SimulatorUncaughtExceptionHandler(Runnable exit) {
    this.exit = exit;
  }

  /**
   * Makes this the default uncaught exception handler, unless one is already set. {@code exit} is
   * run after a crash has been logged.
   */
  static void installIfNone(Runnable exit) {
    if (Thread.getDefaultUncaughtExceptionHandler() == null) {
      Thread.setDefaultUncaughtExceptionHandler(new SimulatorUncaughtExceptionHandler(exit));
    }
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    try {
      Log.e("AndroidRuntime", "FATAL EXCEPTION: " + thread.getName(), throwable);
    } finally {
      exit.run();
    }
  }
}
