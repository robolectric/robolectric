package org.robolectric.android.internal;

import static org.robolectric.Shadows.shadowOf;

import android.app.Instrumentation;
import android.os.Looper;

final class SimulatorInstrumentation extends Instrumentation {

  @Override
  public void waitForIdleSync() {
    shadowOf(Looper.getMainLooper()).idle();
  }

  @Override
  public void runOnMainSync(Runnable runner) {
    waitForIdleSync();
    runner.run();
  }
}
