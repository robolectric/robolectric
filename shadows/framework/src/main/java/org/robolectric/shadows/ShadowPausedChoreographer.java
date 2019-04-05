package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.Choreographer;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * A {@link Choreographer} shadow for {@link LooperMode.Mode.PAUSED}.
 *
 * This shadow is largely a no-op. In {@link LooperMode.Mode.PAUSED} mode, the shadowing is done
 * at a lower level via {@link ShadowDisplayEventReceiver}.
 *
 * This class should not be referenced directly - use {@link ShadowChoreographer} instead.
 */
@Implements(
    value = Choreographer.class,
    shadowPicker = ShadowChoreographer.Picker.class,
    isInAndroidSdk = false)
public class ShadowPausedChoreographer extends ShadowChoreographer {

  @Resetter
  public static void reset() {
    reflector(ChoregrapherReflector.class).getThreadInstance().remove();
  }

  @ForType(Choreographer.class)
  private interface ChoregrapherReflector {

    @Accessor("sThreadInstance")
    @Static
    ThreadLocal<Choreographer> getThreadInstance();
  }
}
