package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.shadows.ShadowSupervisionManager.ACTION_CONFIRM_SUPERVISION_CREDENTIALS;

import android.app.supervision.SupervisionManager;
import android.content.Intent;
import android.os.UserHandle;
import com.android.internal.widget.LockPatternUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public class ShadowSupervisionManagerTest {

  @Test
  public void setSupervisionEnabledForUserFalse_isSupervisionEnabledForUserIsFalse() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager shadowSupervisionManager = Shadow.extract(supervisionManager);

    shadowSupervisionManager.setSupervisionEnabledForUser(/* userId= */ 0, /* enabled= */ false);

    assertThat(supervisionManager.isSupervisionEnabledForUser(/* userId= */ 0)).isFalse();
  }

  @Test
  public void setSupervisionEnabledForUserTrue_isSupervisionEnabledForUserIsTrue() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager shadowSupervisionManager = Shadow.extract(supervisionManager);

    shadowSupervisionManager.setSupervisionEnabledForUser(/* userId= */ 0, /* enabled= */ true);

    assertThat(supervisionManager.isSupervisionEnabledForUser(/* userId= */ 0)).isTrue();
  }

  @Test
  public void setSupervisionEnabledFalse_isSupervisionEnabledForCurrentUserIsFalse() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager unused = Shadow.extract(supervisionManager);

    supervisionManager.setSupervisionEnabled(/* enabled= */ false);

    assertThat(supervisionManager.isSupervisionEnabled()).isFalse();
  }

  @Test
  public void setSupervisionEnabledTrue_isSupervisionEnabledForCurrentUserIsTrue() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager unused = Shadow.extract(supervisionManager);

    supervisionManager.setSupervisionEnabled(/* enabled= */ true);

    assertThat(supervisionManager.isSupervisionEnabled()).isTrue();
  }

  @Test
  public void overrideSupervisionEnabledTrue_isSupervisionEnabledForCurrentUserIsTrue() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager shadowSupervisionManager = Shadow.extract(supervisionManager);

    shadowSupervisionManager.overrideSupervisionEnabled(/* enabled= */ true);

    assertThat(supervisionManager.isSupervisionEnabled()).isTrue();
  }

  @Test
  public void doNotCallSetSupervisionEnabledForUser_isSupervisionEnabledForUserIsFalse() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager unused = Shadow.extract(supervisionManager);

    assertThat(supervisionManager.isSupervisionEnabledForUser(/* userId= */ 0)).isFalse();
  }

  @Test
  public void doNotCallSetSupervisionEnabled_isSupervisionEnabledForCurrentUserIsFalse() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager unused = Shadow.extract(supervisionManager);

    assertThat(supervisionManager.isSupervisionEnabled()).isFalse();
  }

  @Test
  public void supervisionDisabled_createConfirmSupervisionCredentialsIntentIsNull() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager shadowSupervisionManager = Shadow.extract(supervisionManager);

    shadowSupervisionManager.overrideSupervisionEnabled(/* enabled= */ false);
    shadowSupervisionManager.overrideCredentialType(LockPatternUtils.CREDENTIAL_TYPE_PIN);

    assertThat(supervisionManager.createConfirmSupervisionCredentialsIntent()).isNull();
  }

  @Test
  public void userNull_createConfirmSupervisionCredentialsIntentIsNull() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager shadowSupervisionManager = Shadow.extract(supervisionManager);

    shadowSupervisionManager.overrideSupervisionEnabledForUser(
        /* userId= */ UserHandle.USER_NULL, /* enabled= */ true);
    shadowSupervisionManager.overrideCredentialType(LockPatternUtils.CREDENTIAL_TYPE_PIN);

    assertThat(supervisionManager.createConfirmSupervisionCredentialsIntent()).isNull();
  }

  @Test
  public void deviceSecureCredentialTypeNone_createConfirmSupervisionCredentialsIntentIsNull() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager shadowSupervisionManager = Shadow.extract(supervisionManager);

    shadowSupervisionManager.overrideCredentialType(LockPatternUtils.CREDENTIAL_TYPE_NONE);

    assertThat(supervisionManager.createConfirmSupervisionCredentialsIntent()).isNull();
  }

  @Test
  public void deviceSecureCredentialTypePin_createConfirmSupervisionCredentialsIntentIsNotNull() {
    SupervisionManager supervisionManager =
        getApplication().getSystemService(SupervisionManager.class);
    ShadowSupervisionManager shadowSupervisionManager = Shadow.extract(supervisionManager);

    shadowSupervisionManager.overrideSupervisionEnabledForUser(
        /* userId= */ 0, /* enabled= */ true);
    shadowSupervisionManager.overrideCredentialType(LockPatternUtils.CREDENTIAL_TYPE_PIN);
    Intent expectedIntent = new Intent(ACTION_CONFIRM_SUPERVISION_CREDENTIALS);
    expectedIntent.setPackage("com.android.settings");

    assertThat(
            supervisionManager
                .createConfirmSupervisionCredentialsIntent()
                .filterEquals(expectedIntent))
        .isTrue();
  }
}
