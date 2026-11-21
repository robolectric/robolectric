package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.annotation.Filter.Order.BEFORE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.SystemClock;
import android.view.Choreographer;
import android.view.DisplayEventReceiver;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * A shadow for {@link Choreographer} that:
 *
 * <ul>
 *   <li>is based off ShadowPausedChoreographer implementation and does not support
 *       LooperMode.LEGACY
 *   <li>Uses 'no-hardware-vsync' software based frame timing managed in the real Choreographer
 *       implementation
 *   <li>Currently used when running on > BAKLAVA SDKs
 * </ul>
 *
 * <p>The behavior should be close to the existing ShadowPausedChoreographer behavior with the
 * following subtle timing differences:
 *
 * <ul>
 *   <li>The first call to Choreographer.postFrameCallback will be scheduled immediately as opposed
 *       to scheduled after frameDelay
 *   <li>Frame callbacks will always occur at fixed times. For example, using the default 15m frame
 *       delay an animation that is requesting a frame callback on every frame would see them at
 *       100, 115, 130, etc ms, As opposed to the previous implementation that would always schedule
 *       the next frame as 15 ms from the current time. (when in !isPaused() mode)
 *   <li>Effectively uses millisecond precision for frameDelay. With no-vsync mode, Choreographer
 *       uses the Looper to schedule frame callbacks which has ms precision
 * </ul>
 */
@Implements(
    value = Choreographer.class,
    isInAndroidSdk = false,
    minSdk = CINNAMON_BUN,
    shadowPicker = ShadowChoreographer.Picker.class)
public class ShadowPausedNoVsyncChoreographer extends ShadowPausedChoreographer {

  @RealObject private Choreographer realChoreographer;

  /** Whether {@link #doFrame} is running, i.e. a frame is in progress. */
  private boolean inFrame;

  /**
   * The time in ms that the clock is advanced to once the current frame completes, or -1 if no
   * frame was scheduled during it.
   */
  private long nextFrameTimeAfterFrameMs = -1;

  @Implementation
  protected static boolean getUseVsync() {
    return false;
  }

  @Filter(order = BEFORE) // this needs to run before mFrameScheduled is updated
  protected void scheduleFrameLocked(long now) {
    ChoreographerMainReflector choreographerReflector =
        reflector(ChoreographerMainReflector.class, realChoreographer);
    if (!choreographerReflector.getFrameScheduled()) {
      ChoreographerReflector parentReflector =
          reflector(ChoreographerReflector.class, realChoreographer);
      long lastFrameNanos;
      if (getApiLevel() > CINNAMON_BUN && parentReflector.isNoMoreResyncFlagEnabled()) {
        lastFrameNanos =
            ReflectionHelpers.callInstanceMethod(
                parentReflector.getAnimationTime(), "getLastVsyncNanos");
      } else {
        lastFrameNanos = choreographerReflector.getLastFrameTimeNanos();
      }

      final long nextFrameTimeMs =
          Math.max(
              lastFrameNanos / TimeUnit.MILLISECONDS.toNanos(1)
                  + ShadowChoreographer.getFrameDelay().toMillis(),
              now);
      // nextVsyncTime is not optimally named , since this is explictly not using vsync
      ShadowChoreographer.setNextVsyncTimeNanos(TimeUnit.MILLISECONDS.toNanos(nextFrameTimeMs));
      if (!isPaused()) {
        // not paused mode means clock needs to auto advanced to next frame time
        // mirror the logic in upstream to determine when the next frame is scheduled
        if (inFrame && advanceClockAfterFrame()) {
          // A frame scheduled while one is in progress, e.g. by a view that changes during layout,
          // must not move the clock past the current frame before it is drawn.
          nextFrameTimeAfterFrameMs = nextFrameTimeMs;
        } else {
          advanceClockTo(nextFrameTimeMs);
        }
      }
    }
  }

  @Implementation(minSdk = CINNAMON_BUN)
  protected void doFrame(
      long frameTimeNanos, int frame, DisplayEventReceiver.VsyncEventData vsyncEventData) {
    boolean wasInFrame = inFrame;
    inFrame = true;
    try {
      super.doFrame(frameTimeNanos, frame, vsyncEventData);
    } finally {
      inFrame = wasInFrame;
    }
    if (!inFrame && nextFrameTimeAfterFrameMs >= 0) {
      long nextFrameTimeMs = nextFrameTimeAfterFrameMs;
      nextFrameTimeAfterFrameMs = -1;
      if (!isPaused()) {
        advanceClockTo(nextFrameTimeMs);
      }
    }
  }

  /**
   * Returns whether a frame scheduled while another one is in progress advances the clock once the
   * current frame completes. It used to advance the clock as soon as the frame was scheduled, so
   * the rest of the current frame saw the clock one frame delay ahead of the frame time.
   *
   * <p>This is a temporary opt-out to allow teams to migrate slowly: work posted with a delay
   * during a frame, such as timeouts and ripple resets, now becomes due one frame delay earlier,
   * which affects tests that relied on the extra time. Turn it off with {@code
   * -Drobolectric.advanceClockAfterFrame=false}. This property will be removed in a future release.
   */
  private static boolean advanceClockAfterFrame() {
    return Boolean.parseBoolean(System.getProperty("robolectric.advanceClockAfterFrame", "true"));
  }

  private static void advanceClockTo(long timeMs) {
    ShadowPausedSystemClock.advanceBy(timeMs - SystemClock.uptimeMillis(), TimeUnit.MILLISECONDS);
  }

  @ForType(Choreographer.class)
  interface ChoreographerMainReflector {
    @Accessor("mFrameScheduled")
    boolean getFrameScheduled();

    @Accessor("mLastFrameTimeNanos")
    long getLastFrameTimeNanos();
  }
}
