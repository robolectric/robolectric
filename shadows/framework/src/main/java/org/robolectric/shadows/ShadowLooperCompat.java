package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

/**
 * Helper class to delegate to either Scheduler or ControlledLooper APIs.
 *
 * <p>Transient class, do not use
 */
public abstract class ShadowLooperCompat {

  public abstract void idle();

  public abstract void runPaused(Runnable runnable);

  public static ShadowLooperCompat get(Looper looper) {
    if (ControlledLooper.useControlledLooper()) {
      return new ControlledShadowLooperCompat(ControlledLooper.get(looper));
    } else {
      return new DelegateShadowLooperCompat(shadowOf(looper));
    }
  }

  private static class ControlledShadowLooperCompat extends ShadowLooperCompat {
    private final ControlledLooper controlledLooper;

    ControlledShadowLooperCompat(ControlledLooper looper) {
      this.controlledLooper = looper;
    }

    @Override
    public void idle() {
      controlledLooper.idle();
    }

    @Override
    public void runPaused(Runnable r) {
      // ControlledLooper is always paused
      r.run();
    }
  }

  private static class DelegateShadowLooperCompat extends ShadowLooperCompat {
    private final ShadowLooper shadowLooper;

    DelegateShadowLooperCompat(ShadowLooper looper) {
      this.shadowLooper = looper;
    }

    @Override
    public void idle() {
      shadowLooper.idle();
    }

    @Override
    public void runPaused(Runnable r) {
      shadowLooper.runPaused(r);
    }
  }
}
