package org.robolectric.shadows;

import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.hardware.biometrics.BiometricManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit test for {@link ShadowBiometricManager} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowBiometricManagerTest {
  private BiometricManager biometricManager;

  @Before
  public void setUp() {
    biometricManager =
        ApplicationProvider.getApplicationContext().getSystemService(BiometricManager.class);
    assertThat(biometricManager).isNotNull();
  }

  @Test
  public void testCanAuthenticate_serviceNotConnected_canNotAuthenticate() {
    ShadowBiometricManager shadowBiometricManager = Shadow.extract(biometricManager);
    shadowBiometricManager.setCanAuthenticate(false);

    assertThat(biometricManager.canAuthenticate()).isEqualTo(BIOMETRIC_ERROR_NO_HARDWARE);
  }

  @Test
  public void testCanAuthenticate_serviceConnected_canAuthenticate() {
    ShadowBiometricManager shadowBiometricManager = Shadow.extract(biometricManager);
    shadowBiometricManager.setCanAuthenticate(true);

    assertThat(biometricManager.canAuthenticate()).isEqualTo(BIOMETRIC_SUCCESS);
  }

  @Test
  public void testCanAuthenticate_serviceNotConnected_noEnrolledBiometric_biometricNotEnrolled() {
    ShadowBiometricManager shadowBiometricManager = Shadow.extract(biometricManager);
    shadowBiometricManager.setCanAuthenticate(false);
    shadowBiometricManager.setAuthenticatorType(BIOMETRIC_ERROR_NONE_ENROLLED);

    assertThat(biometricManager.canAuthenticate()).isEqualTo(BIOMETRIC_ERROR_NONE_ENROLLED);
  }

  @Test
  public void testCanAuthenticate_serviceNotConnected_noHardware_biometricHwUnavailable() {
    ShadowBiometricManager shadowBiometricManager = Shadow.extract(biometricManager);
    shadowBiometricManager.setCanAuthenticate(false);
    shadowBiometricManager.setAuthenticatorType(BIOMETRIC_ERROR_HW_UNAVAILABLE);

    assertThat(biometricManager.canAuthenticate()).isEqualTo(BIOMETRIC_ERROR_HW_UNAVAILABLE);
  }

  @Test
  @Config(minSdk = R)
  public void
      testCanAuthenticate_serviceNotConnected_securityUpdateRequired_biometricErrorSecurityUpdateRequired() {
    ShadowBiometricManager shadowBiometricManager = Shadow.extract(biometricManager);
    shadowBiometricManager.setCanAuthenticate(false);
    shadowBiometricManager.setAuthenticatorType(BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED);

    assertThat(biometricManager.canAuthenticate())
        .isEqualTo(BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED);
  }

  @Test
  @Config(minSdk = R)
  public void
      testCanAuthenticateBiometricWeak_serviceConnected_noWeakButHaveStrongEntrolled_canAuthenticate() {
    ShadowBiometricManager shadowBiometricManager = Shadow.extract(biometricManager);
    shadowBiometricManager.setCanAuthenticate(true);
    shadowBiometricManager.setAuthenticatorType(BiometricManager.Authenticators.BIOMETRIC_STRONG);

    assertThat(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK))
        .isEqualTo(BIOMETRIC_SUCCESS);
  }

  @Test
  @Config(minSdk = R)
  public void testCanAuthenticateBiometricWeakDeviceCredential_serviceConnected_canAuthenticate() {
    final int authenticators =
        BiometricManager.Authenticators.BIOMETRIC_WEAK
            | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
    ShadowBiometricManager shadowBiometricManager = Shadow.extract(biometricManager);
    shadowBiometricManager.setCanAuthenticate(true);
    shadowBiometricManager.setAuthenticatorType(authenticators);

    assertThat(biometricManager.canAuthenticate(authenticators)).isEqualTo(BIOMETRIC_SUCCESS);
  }
}
