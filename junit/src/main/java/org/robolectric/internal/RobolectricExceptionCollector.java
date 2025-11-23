package org.robolectric.internal;

import org.robolectric.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Thread.UncaughtExceptionHandler} that collects uncaught exceptions from background
 * threads and coroutines and rethrows them the first suppressed exception if needed.
 *
 * <p>Call {@link #install()} to set it as the default handler. At the end of a test, call
 * {@link #throwFirstSuppressedException()} to rethrow the first exception that was collected.
 */
public final class RobolectricExceptionCollector implements Thread.UncaughtExceptionHandler {

  private RobolectricExceptionCollector() {
    // don't make one of me!
  }

  private static RobolectricExceptionCollector INSTANCE;
  private static final List<Throwable> uncaughtExceptions = new ArrayList<>();

  public synchronized static void install() {
    if (INSTANCE == null) {
      INSTANCE = new RobolectricExceptionCollector();
      Thread.setDefaultUncaughtExceptionHandler(INSTANCE);
    }
  }

  public static void throwFirstSuppressedException() {
    if (!uncaughtExceptions.isEmpty()) {
      Util.sneakyThrow(uncaughtExceptions.getFirst());
    }
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    uncaughtExceptions.add(e);
  }
}
