package org.robolectric.shadows;

import android.view.InputEventReceiver;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = InputEventReceiver.class, isInAndroidSdk = false)
public class ShadowInputEventReceiver {
  @Implementation
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  public void consumeBatchedInputEvents(long frameTimeNanos) {
    // The real implementation of this calls a JNI method, and logs a statement if the native
    // object isn't present. Since the native object will never be present in Robolectric tests, it
    // ends up being rather spammy in test logs, so we no-op it.
  }
}
