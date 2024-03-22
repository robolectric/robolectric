package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.view.Choreographer;
import android.view.DisplayEventReceiver;
import com.android.internal.annotations.VisibleForTesting;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowDisplayEventReceiver.DisplayEventReceiverReflector;
import org.robolectric.versioning.AndroidVersions.NMR1;
import org.robolectric.versioning.AndroidVersions.O;
import org.robolectric.versioning.AndroidVersions.T;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * A {@link Choreographer} shadow for {@link LooperMode.Mode.PAUSED}.
 *
 * <p>This shadow is largely a no-op. In {@link LooperMode.Mode.PAUSED} mode, the shadowing is done
 * at a lower level via {@link ShadowDisplayEventReceiver}.
 *
 * <p>This class should not be referenced directly - use {@link ShadowChoreographer} instead.
 */
@Implements(
    value = Choreographer.class,
    shadowPicker = ShadowChoreographer.Picker.class,
    isInAndroidSdk = false)
public class ShadowPausedChoreographer extends ShadowChoreographer {

  // keep track of all Loopers with active Choreographer so they can be selectively reset
  private static final Set<Looper> choreographedLoopers = new CopyOnWriteArraySet<>();

  @RealObject private Choreographer realChoreographer;

  @Implementation(maxSdk = NMR1.SDK_INT)
  protected void __constructor__(Looper looper) {
    reflector(ChoreographerReflector.class, realChoreographer).__constructor__(looper);
    choreographedLoopers.add(looper);
  }

  @Implementation(minSdk = O.SDK_INT, maxSdk = T.SDK_INT)
  protected void __constructor__(Looper looper, int vsyncSource) {
    reflector(ChoreographerReflector.class, realChoreographer).__constructor__(looper, vsyncSource);
    choreographedLoopers.add(looper);
  }

  @Implementation(minSdk = U.SDK_INT)
  protected void __constructor__(Looper looper, int vsyncSource, long layerHandle) {
    reflector(ChoreographerReflector.class, realChoreographer)
        .__constructor__(looper, vsyncSource, layerHandle);
    choreographedLoopers.add(looper);
  }

  /**
   * Resets the choreographer ThreadLocal instance for the given Looper
   *
   * @param looper an active looper whose queue has already been reset
   */
  static void reset(Looper looper) {
    if (choreographedLoopers.remove(looper)) {
      if (looper.getThread() == Thread.currentThread()) {
        reflector(ChoreographerReflector.class).getThreadInstance().remove();
      } else if (looper.getThread().isAlive()) {
        ShadowPausedLooper shadowLooper = Shadow.extract(looper);
        shadowLooper.postSyncQuiet(
            () -> reflector(ChoreographerReflector.class).getThreadInstance().remove());
      }
    }
  }

  // safeguard that clears the list of choreographed Loopers. Intended to clean up references
  // to Loopers that are no longer running
  static void clearLoopers() {
    choreographedLoopers.clear();
  }

  /**
   * Returns true if choreographer has been initialized properly.
   *
   * @return
   */
  @VisibleForTesting
  boolean isInitialized() {
    DisplayEventReceiver receiver =
        reflector(ChoreographerReflector.class, realObject).getReceiver();
    return reflector(DisplayEventReceiverReflector.class, receiver).getReceiverPtr() != 0;
  }
}
