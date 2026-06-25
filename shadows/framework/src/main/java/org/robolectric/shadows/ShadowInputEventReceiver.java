package org.robolectric.shadows;

import android.view.InputEventReceiver;
import dalvik.system.CloseGuard;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(value = InputEventReceiver.class, isInAndroidSdk = false)
public class ShadowInputEventReceiver {

  @ReflectorObject private InputEventReceiverReflector inputEventReceiverReflector;

  @Implementation
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  public void consumeBatchedInputEvents(long frameTimeNanos) {
    // The real implementation of this calls a JNI method, and logs a statement if the native
    // object isn't present. Since the native object will never be present in Robolectric tests, it
    // ends up being rather spammy in test logs, so we no-op it.
  }

  @Filter(order = Order.BEFORE)
  protected void dispose(boolean finalized) {
    CloseGuard closeGuard = inputEventReceiverReflector.getCloseGuard();
    // Suppresses noisy CloseGuard warning
    if (closeGuard != null) {
      closeGuard.close();
    }
  }

  /** Reflector interface for {@link InputEventReceiver}'s internals. */
  @ForType(InputEventReceiver.class)
  interface InputEventReceiverReflector {

    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();
  }
}
