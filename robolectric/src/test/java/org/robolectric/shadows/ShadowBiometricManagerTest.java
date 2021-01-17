package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.hardware.biometrics.BiometricManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Objects;
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
    ((ShadowBiometricManager) Shadow.extract(biometricManager)).setCanAuthenticate(false);

    assertThat(biometricManager.canAuthenticate())
        .isEqualTo(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE);
  }

  @Test
  public void testCanAuthenticate_serviceConnected_canAuthenticate() {
    ((ShadowBiometricManager) Shadow.extract(biometricManager)).setCanAuthenticate(true);

    assertThat(biometricManager.canAuthenticate()).isEqualTo(BiometricManager.BIOMETRIC_SUCCESS);
  }
}
