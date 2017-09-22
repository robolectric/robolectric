package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.app.KeyguardManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(KeyguardManager.class)
public class ShadowKeyguardManager {
  @RealObject private KeyguardManager realKeyguardManager;

  private KeyguardManager.KeyguardLock keyguardLock =
      Shadow.newInstanceOf(KeyguardManager.KeyguardLock.class);

  private boolean inRestrictedInputMode = false;
  private boolean isKeyguardLocked = false;

  /**
   * For tests, returns the value set via {@link #setinRestrictedInputMode(boolean)}, or `false` by
   * default.
   *
   * @see #setinRestrictedInputMode(boolean)
   */
  @Implementation
  public boolean inKeyguardRestrictedInputMode() {
    return inRestrictedInputMode;
  }

  /**
   * For tests, returns the value set via {@link #setKeyguardLocked(boolean)}, or `false` by
   * default.
   *
   * @see #setKeyguardLocked(boolean)
   */
  @Implementation
  public boolean isKeyguardLocked() {
    return isKeyguardLocked;
  }

  /**
   * Sets the value to be returned by {@link #isKeyguardLocked()}.
   *
   * @see #isKeyguardLocked()
   */
  public void setKeyguardLocked(boolean isKeyguardLocked) {
    this.isKeyguardLocked = isKeyguardLocked;
  }

  /**
   * For tests, returns a {@link ShadowKeyguardLock}.
   *
   * @see ShadowKeyguardLock
   */
  @Implementation
  public KeyguardManager.KeyguardLock newKeyguardLock(String tag) {
    return keyguardLock;
  }

  /**
   * Sets the value to be returned by {@link #isKeyguardRestrictedInputMode()}.
   *
   * @see #isKeyguardRestrictedInputMode()
   */
  public void setinRestrictedInputMode(boolean restricted) {
    inRestrictedInputMode = restricted;
  }

  private boolean isKeyguardSecure;

  /**
   * For tests on Android >=M, returns the value set by {@link #setIsKeyguardSecure(boolean)}, or
   * `false` by default.
   *
   * @see #setIsKeyguardSecure(boolean)
   */
  @Implementation(minSdk = M)
  public boolean isKeyguardSecure() {
    return isKeyguardSecure;
  }

  /**
   * For tests on Android >=M, sets the value to be returned by {@link #isKeyguardSecure()}.
   *
   * @see #isKeyguardSecure()
   */
  public void setIsKeyguardSecure(boolean secure) {
    isKeyguardSecure = secure;
  }

  private boolean isDeviceSecure;

  /**
   * For tests on Android >=M, returns the value set by {@link #setIsDeviceSecure(boolean)}, or
   * `false` by default.
   *
   * @see #setIsDeviceSecure(boolean)
   */
  @Implementation(minSdk = M)
  public boolean isDeviceSecure() {
    return isDeviceSecure;
  }

  /**
   * For tests on Android >=M, sets the value to be returned by {@link #isDeviceSecure()}.
   *
   * @see #isDeviceSecure()
   */
  public void setIsDeviceSecure(boolean isDeviceSecure) {
    this.isDeviceSecure = isDeviceSecure;
  }

  /** An implementation of {@link KeyguardManager#KeyguardLock}, for use in tests. */
  @Implements(KeyguardManager.KeyguardLock.class)
  public static class ShadowKeyguardLock {
    private boolean keyguardEnabled = true;

    /**
     * Sets the value to be returned by {@link #isEnabled()} to false.
     *
     * @see #isEnabled()
     */
    @Implementation
    public void disableKeyguard() {
      keyguardEnabled = false;
    }

    /**
     * Sets the value to be returned by {@link #isEnabled()} to true.
     *
     * @see #isEnabled()
     */
    @Implementation
    public void reenableKeyguard() {
      keyguardEnabled = true;
    }

    /**
     * For tests, returns the value set via {@link #disableKeyguard()} or {@link reenableKeyguard},
     * or `true` by default.
     *
     * @see #setKeyguardLocked(boolean)
     */
    public boolean isEnabled() {
      return keyguardEnabled;
    }
  }
}
