package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
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
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * A {@link Choreographer} shadow for {@link LooperMode.Mode#PAUSED}.
 *
 * <p>This shadow is largely a no-op. In {@link LooperMode.Mode#PAUSED} mode, the shadowing is done
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
  protected static final Set<Choreographer> activeChoreographers = new CopyOnWriteArraySet<>();

  @RealObject private Choreographer realChoreographer;

  @Filter(maxSdk = N_MR1, order = Order.AFTER)
  protected void __constructor__(Looper looper) {
    activeChoreographers.add(realChoreographer);
  }

  @Filter(minSdk = O, maxSdk = TIRAMISU, order = Order.AFTER)
  protected void __constructor__(Looper looper, int vsyncSource) {
    activeChoreographers.add(realChoreographer);
  }

  @Filter(minSdk = UPSIDE_DOWN_CAKE, order = Order.AFTER)
  protected void __constructor__(Looper looper, int vsyncSource, long layerHandle) {
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
    if (RuntimeEnvironment.getApiLevel() >= S) {
      choreographerReflector.setLastFrameIntervalNanos(0);
    }
    choreographerReflector.setFrameScheduled(false);
    Object[] /* CallbackQueue */ callbackQueues = choreographerReflector.getCallbackQueues();
    for (Object callbackQueue : callbackQueues) {
      reflector(CallbackQueueReflector.class, callbackQueue).setHead(null);
    }
    choreographerReflector.setCallbackPool(null);
    choreographerReflector.setCallbacksRunning(false);
    if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
      ReflectionHelpers.callInstanceMethod(
          choreographerReflector.getFrameData(),
          "update",
          ClassParameter.from(long.class, 0),
          ClassParameter.from(int.class, 0));
    }

    if (RuntimeEnvironment.getApiLevel() >= Q) {
      Arrays.fill(((FrameInfo) choreographerReflector.getFrameInfo()).frameInfo, 0);
    }
    DisplayEventReceiver receiver =
        reflector(ChoreographerReflector.class, realObject).getReceiver();
    if (receiver != null) {
      ShadowDisplayEventReceiver shadowReceiver = Shadow.extract(receiver);
      shadowReceiver.resetState();
    }
  }

  /**
   * Returns true if choreographer has been initialized properly.
   *
   * @return
   */
  @VisibleForTesting
  boolean isInitialized() {
    return activeChoreographers.contains(realChoreographer);
  }
}
