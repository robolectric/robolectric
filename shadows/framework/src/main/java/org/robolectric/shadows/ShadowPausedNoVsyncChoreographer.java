package org.robolectric.shadows;

import static org.robolectric.annotation.Filter.Order.BEFORE;
import static org.robolectric.util.reflector.Reflector.reflector;
import static org.robolectric.versioning.VersionCalculator.POST_BAKLAVA;

import android.os.SystemClock;
import android.view.Choreographer;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
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
    minSdk = POST_BAKLAVA,
    shadowPicker = ShadowChoreographer.Picker.class)
public class ShadowPausedNoVsyncChoreographer extends ShadowPausedChoreographer {

  @RealObject private Choreographer realChoreographer;

  @Implementation
  protected static boolean getUseVsync() {
    return false;
  }

  @Filter(order = BEFORE) // this needs to run before mFrameScheduled is updated
  protected void scheduleFrameLocked(long now) {
    ChoreographerMainReflector choreographerReflector =
        reflector(ChoreographerMainReflector.class, realChoreographer);
    if (!choreographerReflector.getFrameScheduled()) {
      final long nextFrameTimeMs =
          Math.max(
              choreographerReflector.getLastFrameTimeNanos() / TimeUnit.MILLISECONDS.toNanos(1)
                  + ShadowChoreographer.getFrameDelay().toMillis(),
              now);
      // nextVsyncTime is not optimally named , since this is explictly not using vsync
      ShadowChoreographer.setNextVsyncTimeNanos(TimeUnit.MILLISECONDS.toNanos(nextFrameTimeMs));
      if (!isPaused()) {
        // not paused mode means clock needs to auto advanced to next frame time
        // mirror the logic in upstream to determine when the next frame is scheduled
        ShadowPausedSystemClock.advanceBy(
            nextFrameTimeMs - SystemClock.uptimeMillis(), TimeUnit.MILLISECONDS);
      }
    }
  }

  @ForType(Choreographer.class)
  interface ChoreographerMainReflector {
    @Accessor("mFrameScheduled")
    boolean getFrameScheduled();

    @Accessor("mLastFrameTimeNanos")
    long getLastFrameTimeNanos();
  }
}
