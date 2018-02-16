package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardDismissCallback;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(KeyguardManager.class)
public class ShadowKeyguardManager {
  @RealObject private KeyguardManager realKeyguardManager;

  private KeyguardManager.KeyguardLock keyguardLock =
      Shadow.newInstanceOf(KeyguardManager.KeyguardLock.class);

  private boolean inRestrictedInputMode;
  private boolean isKeyguardLocked;
  private boolean isDeviceLocked;
  private boolean isKeyguardSecure;
  private boolean isDeviceSecure;
  private KeyguardManager.KeyguardDismissCallback callback;

  /**
   * For tests, returns the value set via {@link #setinRestrictedInputMode(boolean)}, or `false` by
   * default.
   *
   * @see #setInRestrictedInputMode(boolean)
   */
  @Implementation
  protected boolean inKeyguardRestrictedInputMode() {
    return inRestrictedInputMode;
  }

  @Implementation(minSdk = O)
  protected void requestDismissKeyguard(
      Activity activity, KeyguardManager.KeyguardDismissCallback callback) {
    if (isKeyguardLocked) {
      if (this.callback != null) {
        callback.onDismissError();
      }
      this.callback = callback;
    } else {
      callback.onDismissError();
    }
  }

  /**
   * For tests, returns the value set via {@link #setKeyguardLocked(boolean)}, or `false` by
   * default.
   *
   * @see #setKeyguardLocked(boolean)
   */
  @Implementation
  protected boolean isKeyguardLocked() {
    return isKeyguardLocked;
  }

  /**
   * Sets whether the device keyguard is locked or not. This affects the value to be returned by
   * {@link #isKeyguardLocked()} and also invokes callbacks set in
   *  {@link KeyguardManager#requestDismissKeyguard()}.
   *
   *  @param isKeyguardLocked true to lock the keyguard. If a KeyguardDismissCallback is set will
   *  fire {@link KeyguardDismissCallback#onDismissCancelled()} or false to unlock and dismiss the
   *  keyguard firing {@link KeyguardDismissCallback#onDismissSucceeded()} if a
   *  KeyguardDismissCallback is set.
   *  */
  public void setKeyguardLocked(boolean isKeyguardLocked) {
    this.isKeyguardLocked = isKeyguardLocked;
    if (callback != null) {
      if (isKeyguardLocked) {
        callback.onDismissCancelled();
      } else {
        callback.onDismissSucceeded();
      }
      callback = null;
    }
  }

  /**
   * For tests, returns a {@link ShadowKeyguardLock}.
   *
   * @see ShadowKeyguardLock
   */
  @Implementation
  protected KeyguardManager.KeyguardLock newKeyguardLock(String tag) {
    return keyguardLock;
  }

  /**
   * Sets the value to be returned by {@link KeyguardManager#inKeyguardRestrictedInputMode()}.
   *
   * @see KeyguardManager#inKeyguardRestrictedInputMode()
   * @deprecated use {@link #setInRestrictedInputMode(boolean)} instead
   */
  @Deprecated
  public void setinRestrictedInputMode(boolean restricted) {
    inRestrictedInputMode = restricted;
  }

  /**
   * Sets the value to be returned by {@link KeyguardManager#inKeyguardRestrictedInputMode()}.
   *
   * @see KeyguardManager#inKeyguardRestrictedInputMode()
   */
  public void setInRestrictedInputMode(boolean restricted) {
    inRestrictedInputMode = restricted;
  }

  /**
   * For tests, returns the value set by {@link #setIsKeyguardSecure(boolean)}, or `false` by
   * default.
   *
   * @see #setIsKeyguardSecure(boolean)
   */
  @Implementation
  protected boolean isKeyguardSecure() {
    return isKeyguardSecure;
  }

  /**
   * Sets the value to be returned by {@link #isKeyguardSecure()}.
   *
   * @see #isKeyguardSecure()
   */
  public void setIsKeyguardSecure(boolean secure) {
    isKeyguardSecure = secure;
  }

  /**
   * For tests on Android >=M, returns the value set by {@link #setIsDeviceSecure(boolean)}, or
   * `false` by default.
   *
   * @see #setIsDeviceSecure(boolean)
   */
  @Implementation(minSdk = M)
  protected boolean isDeviceSecure() {
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

  /**
   * For tests on Android >=L MR1, sets the value to be returned by {@link #isDeviceLocked()}.
   *
   * @see #isDeviceLocked()
   */
  public void setIsDeviceLocked(boolean isDeviceLocked) {
    this.isDeviceLocked = isDeviceLocked;
  }

  /**
   * @return `false` by default, or the value passed to {@link #setIsDeviceLocked(boolean)}.
   * @see #isDeviceLocked()
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean isDeviceLocked() {
    return isDeviceLocked;
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
    protected void disableKeyguard() {
      keyguardEnabled = false;
    }

    /**
     * Sets the value to be returned by {@link #isEnabled()} to true.
     *
     * @see #isEnabled()
     */
    @Implementation
    protected void reenableKeyguard() {
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
