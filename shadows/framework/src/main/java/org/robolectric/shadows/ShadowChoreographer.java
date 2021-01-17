package org.robolectric.shadows;

import static org.robolectric.shadows.ShadowLooper.looperMode;

import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;

/**
 * The shadow API for {@link android.view.Choreographer}.
 *
 * <p>Different shadow implementations will be used depending on the current {@link LooperMode}. See
 * {@link ShadowLegacyChoreographer} and {@link ShadowPausedChoreographer} for details.
 */
@Implements(value = Choreographer.class, shadowPicker = ShadowChoreographer.Picker.class)
public abstract class ShadowChoreographer {

  public static class Picker extends LooperShadowPicker<ShadowChoreographer> {

    public Picker() {
      super(ShadowLegacyChoreographer.class, ShadowPausedChoreographer.class);
    }
  }

  /**
   * Allows application to specify a fixed amount of delay when {@link #postCallback(int, Runnable,
   * Object)} is invoked. The default delay value is 0. This can be used to avoid infinite animation
   * tasks to be spawned when the Robolectric {@link org.robolectric.util.Scheduler} is in {@link
   * org.robolectric.util.Scheduler.IdleState#PAUSED} mode.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}
   */
  public static void setPostCallbackDelay(int delayMillis) {
    ShadowLegacyChoreographer.setPostCallbackDelay(delayMillis);
  }

  /**
   * Allows application to specify a fixed amount of delay when {@link
   * #postFrameCallback(FrameCallback)} is invoked. The default delay value is 0. This can be used
   * to avoid infinite animation tasks to be spawned when in LooperMode PAUSED or {@link
   * org.robolectric.util.Scheduler.IdleState#PAUSED} and displaying an animation.
   */
  public static void setPostFrameCallbackDelay(int delayMillis) {
    if (looperMode() == Mode.PAUSED) {
      ShadowDisplayEventReceiver.setAsyncVsync(delayMillis);
    } else {
      ShadowLegacyChoreographer.setPostFrameCallbackDelay(delayMillis);
    }
  }

  /**
   * Return the current inter-frame interval.
   *
   * <p>Can only be used in {@link LooperMode.Mode.LEGACY}
   *
   * @return Inter-frame interval.
   */
  public static long getFrameInterval() {
    return ShadowLegacyChoreographer.getFrameInterval();
  }

  /**
   * Set the inter-frame interval used to advance the clock. By default, this is set to 1ms.
   *
   * <p>Only supported in {@link LooperMode.Mode.LEGACY}
   *
   * @param frameInterval Inter-frame interval.
   */
  public static void setFrameInterval(long frameInterval) {
    ShadowLegacyChoreographer.setFrameInterval(frameInterval);
  }
}
