package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardDismissCallback;
import android.content.Intent;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(KeyguardManager.class)
public class ShadowKeyguardManager {
  // These have to be static because on Android L and below, a new instance of KeyguardManager is
  // created each time it is requested.
  private static final Set<Integer> deviceLockedForUsers = new HashSet<Integer>();
  private static final Set<Integer> deviceSecureForUsers = new HashSet<Integer>();
  private static boolean inRestrictedInputMode;
  private static boolean isKeyguardLocked;
  private static boolean isDeviceLocked;
  private static boolean isKeyguardSecure;
  private static boolean isDeviceSecure;
  private static Intent confirmFactoryResetCredentialIntent;
  private static KeyguardManager.KeyguardDismissCallback callback;

  /**
   * For tests, returns the value set via {@link #setinRestrictedInputMode(boolean)}, or false by
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
      if (ShadowKeyguardManager.callback != null) {
        callback.onDismissError();
      }
      ShadowKeyguardManager.callback = callback;
    } else {
      callback.onDismissError();
    }
  }

  /**
   * For tests, returns the value set via {@link #setKeyguardLocked(boolean)}, or false by default.
   *
   * @see #setKeyguardLocked(boolean)
   */
  @Implementation
  protected boolean isKeyguardLocked() {
    return isKeyguardLocked;
  }

  /**
   * Sets whether the device keyguard is locked or not. This affects the value to be returned by
   * {@link #isKeyguardLocked()} and also invokes callbacks set in {@link
   * KeyguardManager#requestDismissKeyguard(Activity, KeyguardDismissCallback)} ()}.
   *
   * @param isKeyguardLocked true to lock the keyguard. If a KeyguardDismissCallback is set will
   *     fire {@link KeyguardDismissCallback#onDismissCancelled()} or false to unlock and dismiss
   *     the keyguard firing {@link KeyguardDismissCallback#onDismissSucceeded()} if a
   *     KeyguardDismissCallback is set.
   */
  public void setKeyguardLocked(boolean isKeyguardLocked) {
    ShadowKeyguardManager.isKeyguardLocked = isKeyguardLocked;
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
   * For tests, returns the value set by {@link #setIsKeyguardSecure(boolean)}, or false by default.
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
   * false by default.
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
    ShadowKeyguardManager.isDeviceSecure = isDeviceSecure;
  }

  /**
   * For tests on Android >=M, returns the value set by {@link #setIsDeviceSecure(int, boolean)}, or
   * false by default.
   *
   * @see #setIsDeviceSecure(int, boolean)
   */
  @Implementation(minSdk = M)
  protected boolean isDeviceSecure(int userId) {
    return deviceSecureForUsers.contains(userId);
  }

  /**
   * For tests on Android >=M, sets the value to be returned by {@link #isDeviceSecure(int)}.
   *
   * @see #isDeviceSecure(int)
   */
  public void setIsDeviceSecure(int userId, boolean isDeviceSecure) {
    if (isDeviceSecure) {
      deviceSecureForUsers.add(userId);
    } else {
      deviceSecureForUsers.remove(userId);
    }
  }

  /**
   * For tests on Android >=L MR1, sets the value to be returned by {@link #isDeviceLocked()}.
   *
   * @see #isDeviceLocked()
   */
  public void setIsDeviceLocked(boolean isDeviceLocked) {
    ShadowKeyguardManager.isDeviceLocked = isDeviceLocked;
  }

  /**
   * @return false by default, or the value passed to {@link #setIsDeviceLocked(boolean)}.
   * @see #isDeviceLocked()
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean isDeviceLocked() {
    return isDeviceLocked;
  }

  /**
   * For tests on Android >= L MR1, sets the value to be returned by {@link #isDeviceLocked(int)}.
   *
   * @see #isDeviceLocked(int)
   */
  public void setIsDeviceLocked(int userId, boolean isLocked) {
    if (isLocked) {
      deviceLockedForUsers.add(userId);
    } else {
      deviceLockedForUsers.remove(userId);
    }
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean isDeviceLocked(int userId) {
    return deviceLockedForUsers.contains(userId);
  }

  /**
   * For tests on Android >= O MR1, sets the value to be returned by
   * {@link #createConfirmFactoryResetCredentialIntent(CharSequence,CharSequence,CharSequence)}.
   *
   * @see #createConfirmFactoryResetCredentialIntent(CharSequence,CharSequence,CharSequence)
   */
  public void setConfirmFactoryResetCredentialIntent(Intent intent) {
    confirmFactoryResetCredentialIntent = intent;
  }

  /**
   * Returns the intent set via
   * {@link #setConfirmFactoryResetCredentialIntent(Intent)}, otherwise null.
   */
  @Implementation(minSdk = O_MR1)
  protected Intent createConfirmFactoryResetCredentialIntent(
      CharSequence title, CharSequence description, CharSequence alternateButtonLabel) {
    return confirmFactoryResetCredentialIntent;
  }

  /** An implementation of {@link KeyguardManager.KeyguardLock}, for use in tests. */
  @Implements(KeyguardManager.KeyguardLock.class)
  public static class ShadowKeyguardLock {
    private static boolean keyguardEnabled = true;

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
     * For tests, returns the value set via {@link #disableKeyguard()} or {@link
     * #reenableKeyguard()}, or true by default.
     *
     * @see #setKeyguardLocked(boolean)
     */
    public boolean isEnabled() {
      return keyguardEnabled;
    }

    @Resetter
    public static void reset() {
      keyguardEnabled = true;
    }
  }

  @Resetter
  public static void reset() {
    deviceLockedForUsers.clear();
    deviceSecureForUsers.clear();
    inRestrictedInputMode = false;
    isKeyguardLocked = false;
    isDeviceLocked = false;
    isKeyguardSecure = false;
    isDeviceSecure = false;
    callback = null;
  }
}
