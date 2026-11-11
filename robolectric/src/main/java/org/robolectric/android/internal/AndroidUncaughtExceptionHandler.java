package org.robolectric.android.internal;

import android.util.Log;
import javax.annotation.Nullable;

/**
 * The default uncaught exception handler that Robolectric installs before the application is
 * created. On Android, {@code RuntimeInit} installs one in every app process, so {@link
 * Thread#getDefaultUncaughtExceptionHandler()} is never null and crash reporters can wrap it. Like
 * Android's, it logs the crash as a "FATAL EXCEPTION" under the AndroidRuntime tag. It then hands
 * the crash to the handler that was installed before it or, if there was none, prints it as the JVM
 * does.
 */
final class AndroidUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

  @Nullable private final Thread.UncaughtExceptionHandler previous;

  AndroidUncaughtExceptionHandler(@Nullable Thread.UncaughtExceptionHandler previous) {
    this.previous = previous;
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    try {
      Log.e("AndroidRuntime", "FATAL EXCEPTION: " + thread.getName(), throwable);
    } finally {
      if (previous != null) {
        previous.uncaughtException(thread, throwable);
      } else {
        System.err.print("Exception in thread \"" + thread.getName() + "\" ");
        throwable.printStackTrace(System.err);
      }
    }
  }
}
