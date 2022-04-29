package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadows.ShadowLooper.looperMode;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import java.time.Duration;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * The shadow API for {@link android.view.Choreographer}.
 *
 * <p>Different shadow implementations will be used depending on the current {@link LooperMode}. See
 * {@link ShadowLegacyChoreographer} and {@link ShadowPausedChoreographer} for details.
 */
@Implements(value = Choreographer.class, shadowPicker = ShadowChoreographer.Picker.class)
public abstract class ShadowChoreographer {

  @RealObject Choreographer realObject;
  private ChoreographerReflector reflector;

  private static volatile boolean isPaused = false;
  private static volatile Duration frameDelay = Duration.ofMillis(1);

  public static class Picker extends LooperShadowPicker<ShadowChoreographer> {

    public Picker() {
      super(ShadowLegacyChoreographer.class, ShadowPausedChoreographer.class);
    }
  }

  /**
   * Sets the delay between each frame. Note that the frames use the {@link ShadowSystemClock} and
   * so have the same fidelity, when using the paused looper mode (which is the only mode supported
   * by {@code ShadowDisplayEventReceiver}) the clock has millisecond fidelity.
   *
   * <p>Reasonable delays may be 15ms (approximating 60fps ~16.6ms), 10ms (approximating 90fps
   * ~11.1ms), and 30ms (approximating 30fps ~33.3ms). Choosing too small of a frame delay may
   * increase runtime as animation frames will have more steps.
   *
   * <p>Only works in {@link LooperMode.Mode#PAUSED} looper mode.
   */
  public static void setFrameDelay(Duration delay) {
    checkState(ShadowLooper.looperMode().equals(Mode.PAUSED), "Looper must be %s", Mode.PAUSED);
    frameDelay = delay;
  }

  /** See {@link #setFrameDelay(Duration)}. */
  public static Duration getFrameDelay() {
    checkState(ShadowLooper.looperMode().equals(Mode.PAUSED), "Looper must be %s", Mode.PAUSED);
    return frameDelay;
  }

  /**
   * Sets whether posting a frame should auto advance the clock or not. When paused the clock is not
   * auto advanced, when unpaused the clock is advanced by the frame delay every time a frame
   * callback is added. The default is not paused.
   *
   * <p>Only works in {@link LooperMode.Mode#PAUSED} looper mode.
   */
  public static void setPaused(boolean paused) {
    checkState(ShadowLooper.looperMode().equals(Mode.PAUSED), "Looper must be %s", Mode.PAUSED);
    isPaused = paused;
  }

  /** See {@link #setPaused(boolean)}. */
  public static boolean isPaused() {
    checkState(ShadowLooper.looperMode().equals(Mode.PAUSED), "Looper must be %s", Mode.PAUSED);
    return isPaused;
  }

  /**
   * Allows application to specify a fixed amount of delay when {@link #postCallback(int, Runnable,
   * Object)} is invoked. The default delay value is 0. This can be used to avoid infinite animation
   * tasks to be spawned when the Robolectric {@link org.robolectric.util.Scheduler} is in {@link
   * org.robolectric.util.Scheduler.IdleState#PAUSED} mode.
   *
   * <p>Only supported in {@link LooperMode.Mode#LEGACY}
   *
   * @deprecated Use the {@link Mode#PAUSED} looper instead.
   */
  @Deprecated
  public static void setPostCallbackDelay(int delayMillis) {
    checkState(ShadowLooper.looperMode().equals(Mode.LEGACY), "Looper must be %s", Mode.LEGACY);
    ShadowLegacyChoreographer.setPostCallbackDelay(delayMillis);
  }

  /**
   * Allows application to specify a fixed amount of delay when {@link
   * #postFrameCallback(FrameCallback)} is invoked. The default delay value is 0. This can be used
   * to avoid infinite animation tasks to be spawned when in LooperMode PAUSED or {@link
   * org.robolectric.util.Scheduler.IdleState#PAUSED} and displaying an animation.
   *
   * @deprecated Use the {@link Mode#PAUSED} looper and {@link #setPaused(boolean)} and {@link
   *     #setFrameDelay(Duration)} to configure the vsync event behavior.
   */
  @Deprecated
  public static void setPostFrameCallbackDelay(int delayMillis) {
    if (looperMode() == Mode.PAUSED) {
      setPaused(delayMillis != 0);
      setFrameDelay(Duration.ofMillis(delayMillis == 0 ? 1 : delayMillis));
    } else {
      ShadowLegacyChoreographer.setPostFrameCallbackDelay(delayMillis);
    }
  }

  /**
   * Return the current inter-frame interval.
   *
   * <p>Can only be used in {@link LooperMode.Mode#LEGACY}
   *
   * @return Inter-frame interval.
   * @deprecated Use the {@link Mode#PAUSED} looper and {@link #getFrameDelay()} to configure the
   *     frame delay.
   */
  @Deprecated
  public static long getFrameInterval() {
    checkState(ShadowLooper.looperMode().equals(Mode.LEGACY), "Looper must be %s", Mode.LEGACY);
    return ShadowLegacyChoreographer.getFrameInterval();
  }

  /**
   * Set the inter-frame interval used to advance the clock. By default, this is set to 1ms.
   *
   * <p>Only supported in {@link LooperMode.Mode#LEGACY}
   *
   * @param frameInterval Inter-frame interval.
   * @deprecated Use the {@link Mode#PAUSED} looper and {@link #setFrameDelay(Duration)} to
   *     configure the frame delay.
   */
  @Deprecated
  public static void setFrameInterval(long frameInterval) {
    checkState(ShadowLooper.looperMode().equals(Mode.LEGACY), "Looper must be %s", Mode.LEGACY);
    ShadowLegacyChoreographer.setFrameInterval(frameInterval);
  }

  @Implementation(maxSdk = R)
  protected void doFrame(long frameTimeNanos, int frame) {
    if (reflector == null) {
      reflector = reflector(ChoreographerReflector.class, realObject);
    }
    PerfStatsCollector.getInstance()
        .measure("doFrame", () -> reflector.doFrame(frameTimeNanos, frame));
  }

  @Resetter
  public static void reset() {
    isPaused = false;
    frameDelay = Duration.ofMillis(1);
  }

  /** Accessor interface for {@link Choreographer}'s internals */
  @ForType(Choreographer.class)
  protected interface ChoreographerReflector {
    @Accessor("sThreadInstance")
    @Static
    ThreadLocal<Choreographer> getThreadInstance();

    @Direct
    void doFrame(long frameTimeNanos, int frame);
  }
}
