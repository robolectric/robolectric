package org.robolectric.shadows;

import android.annotation.IntDef;
import android.app.supervision.SupervisionManager;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import com.android.internal.widget.LockPatternUtils;
import com.google.common.annotations.VisibleForTesting;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Adds Robolectric support for SupervisionManager. */
@Implements(
    value = SupervisionManager.class,
    minSdk = VERSION_CODES.BAKLAVA,
    isInAndroidSdk = false)
public final class ShadowSupervisionManager {
  private final Map<Integer, Boolean> isSupervisionEnabledForUser = new HashMap<>();
  private int credentialType = LockPatternUtils.CREDENTIAL_TYPE_PIN;

  @VisibleForTesting
  static final String ACTION_CONFIRM_SUPERVISION_CREDENTIALS =
      "android.app.supervision.action.CONFIRM_SUPERVISION_CREDENTIALS";

  /** Credential types for the device. */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    LockPatternUtils.CREDENTIAL_TYPE_NONE,
    LockPatternUtils.CREDENTIAL_TYPE_PATTERN,
    LockPatternUtils.CREDENTIAL_TYPE_PIN,
    LockPatternUtils.CREDENTIAL_TYPE_PASSWORD
  })
  private @interface CredentialType {}

  @Implementation
  protected boolean isSupervisionEnabledForUser(int userId) {
    return isSupervisionEnabledForUser.getOrDefault(userId, false);
  }

  @Implementation
  protected void setSupervisionEnabledForUser(int userId, boolean enabled) {
    this.isSupervisionEnabledForUser.put(userId, enabled);
  }

  @Implementation
  protected Intent createConfirmSupervisionCredentialsIntent() {
    if (!isSupervisionEnabledForUser(UserHandle.myUserId())) {
      return null;
    }

    if (UserHandle.myUserId() == UserHandle.USER_NULL || !isDeviceSecure()) {
      return null;
    }

    final Intent intent = new Intent(ACTION_CONFIRM_SUPERVISION_CREDENTIALS);
    // explicitly set the package for security
    intent.setPackage("com.android.settings");

    return intent;
  }

  private boolean isDeviceSecure() {
    return credentialType != LockPatternUtils.CREDENTIAL_TYPE_NONE;
  }

  /**
   * This method overrides the supervision enabled state for the current user. It is public because
   * the setSupervisionEnabledForUser method is not.
   */
  public void overrideSupervisionEnabled(boolean enabled) {
    setSupervisionEnabledForUser(UserHandle.myUserId(), enabled);
  }

  /**
   * This method overrides the supervision enabled state for a specific user. It is public because
   * the setSupervisionEnabledForUser method is not.
   */
  public void overrideSupervisionEnabledForUser(int userId, boolean enabled) {
    setSupervisionEnabledForUser(userId, enabled);
  }

  /**
   * This method overrides the device secure credential type. It is public because setting the
   * device secure credential type is not possible through the SupervisionManager API.
   */
  public void overrideCredentialType(@CredentialType int credentialType) {
    this.credentialType = credentialType;
  }
}
