package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.security.Signature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = M, maxSdk = BAKLAVA)
public class ShadowFingerprintManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Test
  public void authenticate_success() {
    FingerprintManager manager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    ShadowFingerprintManager shadowFingerprintManager = Shadow.extract(manager);

    AuthenticationCallback mockCallback = mock(AuthenticationCallback.class);

    CryptoObject cryptoObject = new CryptoObject(mock(Signature.class));

    manager.authenticate(cryptoObject, null, 0, mockCallback, null);

    shadowFingerprintManager.authenticationSucceeds();

    ArgumentCaptor<AuthenticationResult> result =
        ArgumentCaptor.forClass(AuthenticationResult.class);
    verify(mockCallback).onAuthenticationSucceeded(result.capture());

    assertThat(result.getValue().getCryptoObject()).isEqualTo(cryptoObject);
  }

  @Test
  public void authenticate_failure() {
    FingerprintManager manager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    ShadowFingerprintManager shadowFingerprintManager = Shadow.extract(manager);

    AuthenticationCallback mockCallback = mock(AuthenticationCallback.class);

    CryptoObject cryptoObject = new CryptoObject(mock(Signature.class));

    manager.authenticate(cryptoObject, null, 0, mockCallback, null);

    shadowFingerprintManager.authenticationFails();

    verify(mockCallback).onAuthenticationFailed();
  }

  @Test
  public void hasEnrolledFingerprints() {
    FingerprintManager manager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    ShadowFingerprintManager shadowFingerprintManager = Shadow.extract(manager);
    assertThat(manager.hasEnrolledFingerprints()).isFalse();

    shadowFingerprintManager.setHasEnrolledFingerprints(true);

    assertThat(manager.hasEnrolledFingerprints()).isTrue();
  }

  @Test
  public void setDefaultFingerprints() {
    FingerprintManager manager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    ShadowFingerprintManager shadowFingerprintManager = Shadow.extract(manager);
    assertThat(shadowFingerprintManager.getEnrolledFingerprints()).isEmpty();

    shadowFingerprintManager.setDefaultFingerprints(1);
    assertThat(manager.getEnrolledFingerprints().get(0).getName().toString())
        .isEqualTo("Fingerprint 0");

    assertThat(shadowFingerprintManager.getFingerprintId(0)).isEqualTo(0);
    assertThat(manager.hasEnrolledFingerprints()).isTrue();

    shadowFingerprintManager.setDefaultFingerprints(0);
    assertThat(manager.getEnrolledFingerprints()).isEmpty();
    assertThat(manager.hasEnrolledFingerprints()).isFalse();
  }

  @Test
  public void setHasEnrolledFingerprints_shouldSetNumberOfFingerprints() {
    FingerprintManager manager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    ShadowFingerprintManager shadowFingerprintManager = Shadow.extract(manager);
    assertThat(shadowFingerprintManager.getEnrolledFingerprints()).isEmpty();

    shadowFingerprintManager.setHasEnrolledFingerprints(true);

    assertThat(manager.getEnrolledFingerprints()).hasSize(1);
    assertThat(manager.hasEnrolledFingerprints()).isTrue();

    shadowFingerprintManager.setHasEnrolledFingerprints(false);
    assertThat(manager.getEnrolledFingerprints()).isEmpty();
    assertThat(manager.hasEnrolledFingerprints()).isFalse();
  }

  @Test
  public void isHardwareDetected() {
    FingerprintManager manager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    ShadowFingerprintManager shadowFingerprintManager = Shadow.extract(manager);
    assertThat(manager.isHardwareDetected()).isFalse();

    shadowFingerprintManager.setIsHardwareDetected(true);

    assertThat(manager.isHardwareDetected()).isTrue();
  }

  @Test
  @Config(sdk = VERSION_CODES.S)
  public void getSensorPropertiesInternal_notNull() {
    FingerprintManager manager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    assertThat(manager.getSensorPropertiesInternal()).isNotNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void fingerprintManager_activityContextEnabled_differentInstancesHaveConsistentState() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      FingerprintManager applicationFingerprintManager =
          (FingerprintManager)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.FINGERPRINT_SERVICE);

      Activity activity = controller.get();
      FingerprintManager activityFingerprintManager =
          (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);

      boolean isApplicationFingerprintAvailable =
          applicationFingerprintManager.isHardwareDetected();
      boolean isActivityFingerprintAvailable = activityFingerprintManager.isHardwareDetected();
      assertThat(isActivityFingerprintAvailable).isEqualTo(isApplicationFingerprintAvailable);

      boolean hasApplicationEnrolledFingerprints =
          applicationFingerprintManager.hasEnrolledFingerprints();
      boolean hasActivityEnrolledFingerprints =
          activityFingerprintManager.hasEnrolledFingerprints();
      assertThat(hasActivityEnrolledFingerprints).isEqualTo(hasApplicationEnrolledFingerprints);
    }
  }
}
