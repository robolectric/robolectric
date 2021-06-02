package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.view.InputEventReceiver;
import dalvik.system.CloseGuard;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;

@Implements(value = InputEventReceiver.class, isInAndroidSdk = false)
public class ShadowInputEventReceiver {

  @RealObject protected InputEventReceiver receiver;

  @Implementation
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  public void consumeBatchedInputEvents(long frameTimeNanos) {
    // The real implementation of this calls a JNI method, and logs a statement if the native
    // object isn't present. Since the native object will never be present in Robolectric tests, it
    // ends up being rather spammy in test logs, so we no-op it.
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected void dispose(boolean finalized) {
    CloseGuard closeGuard =
        Reflector.reflector(InputEventReceiverReflector.class, receiver).getCloseGuard();
    // Suppresses noisy CloseGuard warning
    if (closeGuard != null) {
      closeGuard.close();
    }
    directlyOn(
        receiver,
        InputEventReceiver.class,
        "dispose",
        ClassParameter.from(boolean.class, finalized));
  }

  /** Accessor interface for {@link InputEventReceiver}'s internals. */
  @ForType(InputEventReceiver.class)
  interface InputEventReceiverReflector {

    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();
  }
}
