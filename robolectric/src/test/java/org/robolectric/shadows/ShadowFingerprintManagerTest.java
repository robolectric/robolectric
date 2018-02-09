package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import java.security.Signature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = M)
public class ShadowFingerprintManagerTest {

  private FingerprintManager manager;

  @Before
  public void setUp() {
    manager = (FingerprintManager) RuntimeEnvironment.application
        .getSystemService(Context.FINGERPRINT_SERVICE);
  }

  @Test
  public void authenticate_success() {

    AuthenticationCallback mockCallback = mock(AuthenticationCallback.class);

    CryptoObject cryptoObject = new CryptoObject(mock(Signature.class));

    manager.authenticate(cryptoObject, null, 0, mockCallback, null);

    shadowOf(manager).authenticationSucceeds();

    ArgumentCaptor<AuthenticationResult> result = ArgumentCaptor.forClass(AuthenticationResult.class);
    verify(mockCallback).onAuthenticationSucceeded(result.capture());

    assertThat(result.getValue().getCryptoObject()).isEqualTo(cryptoObject);
  }

  @Test
  public void authenticate_failure() {

    AuthenticationCallback mockCallback = mock(AuthenticationCallback.class);

    CryptoObject cryptoObject = new CryptoObject(mock(Signature.class));

    manager.authenticate(cryptoObject, null, 0, mockCallback, null);

    shadowOf(manager).authenticationFails();

    verify(mockCallback).onAuthenticationFailed();
  }

  @Test
  public void hasEnrolledFingerprints() {
    assertThat(manager.hasEnrolledFingerprints()).isFalse();

    shadowOf(manager).setHasEnrolledFingerprints(true);

    assertThat(manager.hasEnrolledFingerprints()).isTrue();
  }

  @Test
  public void isHardwareDetected() {
    assertThat(manager.isHardwareDetected()).isFalse();

    shadowOf(manager).setIsHardwareDetected(true);

    assertThat(manager.isHardwareDetected()).isTrue();
  }
}
