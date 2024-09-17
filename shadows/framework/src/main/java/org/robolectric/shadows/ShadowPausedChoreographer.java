package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.FrameInfo;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.view.Choreographer;
import android.view.DisplayEventReceiver;
import com.android.internal.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowDisplayEventReceiver.DisplayEventReceiverReflector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.versioning.AndroidVersions.NMR1;
import org.robolectric.versioning.AndroidVersions.O;
import org.robolectric.versioning.AndroidVersions.Q;
import org.robolectric.versioning.AndroidVersions.S;
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

  // keep track of all active Choreographers so they can be selectively reset
  private static final Set<Choreographer> activeChoreographers = new CopyOnWriteArraySet<>();

  @RealObject private Choreographer realChoreographer;

  @Implementation(maxSdk = NMR1.SDK_INT)
  protected void __constructor__(Looper looper) {
    reflector(ChoreographerReflector.class, realChoreographer).__constructor__(looper);
    activeChoreographers.add(realChoreographer);
  }

  @Implementation(minSdk = O.SDK_INT, maxSdk = T.SDK_INT)
  protected void __constructor__(Looper looper, int vsyncSource) {
    reflector(ChoreographerReflector.class, realChoreographer).__constructor__(looper, vsyncSource);
    activeChoreographers.add(realChoreographer);
  }

  @Implementation(minSdk = U.SDK_INT)
  protected void __constructor__(Looper looper, int vsyncSource, long layerHandle) {
    reflector(ChoreographerReflector.class, realChoreographer)
        .__constructor__(looper, vsyncSource, layerHandle);
    activeChoreographers.add(realChoreographer);
  }

  @Implementation(minSdk = VERSION_CODES.N)
  protected void dispose() {
    activeChoreographers.remove(realChoreographer);
  }

  /**
   * Resets the Choreographer state
   *
   * <p>Called from ShadowPausedLooper reset to ensure this occurs before Loopers are reset.
   */
  static void resetChoreographers() {
    for (Choreographer choreographer : activeChoreographers) {
      Looper looper = reflector(ChoreographerReflector.class, choreographer).getLooper();
      ShadowPausedChoreographer shadowChoreographer = Shadow.extract(choreographer);
      if (looper.getThread() == Thread.currentThread()) {
        shadowChoreographer.resetState();
      } else if (looper.getThread().isAlive()) {
        ShadowPausedLooper shadowLooper = Shadow.extract(looper);
        shadowLooper.postSyncQuiet(shadowChoreographer::resetState);
      }
    }
  }

  private void resetState() {
    ChoreographerReflector choreographerReflector =
        reflector(ChoreographerReflector.class, realChoreographer);
    choreographerReflector.setLastFrameTimeNanos(Long.MIN_VALUE);
    if (RuntimeEnvironment.getApiLevel() >= S.SDK_INT) {
      choreographerReflector.setLastFrameIntervalNanos(0);
    }
    choreographerReflector.setFrameScheduled(false);
    Object[] /* CallbackQueue */ callbackQueues = choreographerReflector.getCallbackQueues();
    for (Object callbackQueue : callbackQueues) {
      reflector(CallbackQueueReflector.class, callbackQueue).setHead(null);
    }
    choreographerReflector.setCallbackPool(null);
    choreographerReflector.setCallbacksRunning(false);
    if (RuntimeEnvironment.getApiLevel() >= U.SDK_INT) {
      ReflectionHelpers.callInstanceMethod(
          choreographerReflector.getFrameData(),
          "update",
          ClassParameter.from(long.class, 0),
          ClassParameter.from(int.class, 0));
    }

    if (RuntimeEnvironment.getApiLevel() >= Q.SDK_INT) {
      Arrays.fill(((FrameInfo) choreographerReflector.getFrameInfo()).frameInfo, 0);
    }
    DisplayEventReceiver receiver =
        reflector(ChoreographerReflector.class, realObject).getReceiver();
    ShadowDisplayEventReceiver shadowReceiver = Shadow.extract(receiver);
    shadowReceiver.resetState();
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
