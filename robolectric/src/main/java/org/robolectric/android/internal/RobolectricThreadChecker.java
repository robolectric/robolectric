package org.robolectric.android.internal;

import static com.google.common.base.Preconditions.checkState;

import android.os.Looper;
import androidx.test.internal.platform.ThreadChecker;
import org.robolectric.shadows.ShadowLooper;

/**
 * Performs thread checking when in INSTRUMENTATION_TEST Looper Mode where the test thread is
 * distinct from the main thread. No-op for other modes because everything is executed on the main
 * thread (except for manually created worker threads).
 */
@SuppressWarnings("RestrictTo")
public class RobolectricThreadChecker implements ThreadChecker {
  @Override
  public void checkMainThread() {
    if (ShadowLooper.hasTestThread()) {
      checkState(
          Thread.currentThread().equals(Looper.getMainLooper().getThread()),
          "Method cannot be called off the main application thread (on: %s) when running in"
              + " LooperMode.%s",
          Thread.currentThread().getName(),
          ShadowLooper.looperMode());
    }
  }

  @Override
  public void checkNotMainThread() {
    if (ShadowLooper.hasTestThread()) {
      checkState(
          !Thread.currentThread().equals(Looper.getMainLooper().getThread()),
          "Method cannot be called on the main application thread (on: %s) when running in"
              + " LooperMode.%s",
          Thread.currentThread().getName(),
          ShadowLooper.looperMode());
    }
  }
}
