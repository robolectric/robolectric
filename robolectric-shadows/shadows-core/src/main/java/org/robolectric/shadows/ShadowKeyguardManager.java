package org.robolectric.shadows;

import android.app.KeyguardManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.Shadow;

/**
 * Shadow for {@link android.app.KeyguardManager}.
 */
@Implements(KeyguardManager.class)
public class ShadowKeyguardManager {
  @RealObject private KeyguardManager realKeyguardManager;

  private KeyguardManager.KeyguardLock keyguardLock = Shadow.newInstanceOf(KeyguardManager.KeyguardLock.class);

  private boolean inRestrictedInputMode = false;

  @Implementation
  public boolean inKeyguardRestrictedInputMode() {
    return inRestrictedInputMode;
  }

  @Implementation
  public KeyguardManager.KeyguardLock newKeyguardLock(String tag) {
    return keyguardLock;
  }

  public void setinRestrictedInputMode(boolean restricted) {
    inRestrictedInputMode = restricted;
  }

  @Implements(KeyguardManager.KeyguardLock.class)
  public static class ShadowKeyguardLock {
    private boolean keyguardEnabled = true;

    @Implementation
    public void disableKeyguard() {
      keyguardEnabled = false;
    }

    @Implementation
    public void reenableKeyguard() {
      keyguardEnabled = true;
    }

    public boolean isEnabled() {
      return keyguardEnabled;
    }
  }
}
