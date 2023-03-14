package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.Choreographer;
import android.view.DisplayEventReceiver;
import androidx.annotation.VisibleForTesting;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowDisplayEventReceiver.DisplayEventReceiverReflector;

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

  @Resetter
  public static void reset() {
    reflector(ChoreographerReflector.class).getThreadInstance().remove();
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
