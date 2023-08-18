package org.robolectric.android.internal;

import static com.google.common.base.Preconditions.checkState;

import android.os.Looper;
import androidx.test.internal.platform.ThreadChecker;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;

/**
 * Performs thread checking when in INSTRUMENTAION_TEST Looper Mode where the test thread is
 * distinct from the main thread. No-op for other modes because everything is executed on the main
 * thread (except for manually created worker threads).
 */
@SuppressWarnings("RestrictTo")
public class RobolectricThreadChecker implements ThreadChecker {
  @Override
  public void checkMainThread() {
    if (ShadowLooper.looperMode() == LooperMode.Mode.INSTRUMENTATION_TEST) {
      checkState(
          Thread.currentThread().equals(Looper.getMainLooper().getThread()),
          "Method cannot be called off the main application thread (on: %s) when running in"
              + " LooperMode.INSTRUMENTATION_TEST",
          Thread.currentThread().getName());
    }
  }

  @Override
  public void checkNotMainThread() {
    if (ShadowLooper.looperMode() == LooperMode.Mode.INSTRUMENTATION_TEST) {
      checkState(
          !Thread.currentThread().equals(Looper.getMainLooper().getThread()),
          "Method cannot be called on the main application thread (on: %s) when running in"
              + " LooperMode.INSTRUMENTATION_TEST",
          Thread.currentThread().getName());
    }
  }
}
