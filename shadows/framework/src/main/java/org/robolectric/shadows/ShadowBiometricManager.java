package org.robolectric.shadows;

import static android.Manifest.permission.USE_BIOMETRIC;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS;
import static android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE;
import static org.robolectric.shadows.ShadowBuild.Q;

import android.annotation.RequiresPermission;
import android.hardware.biometrics.BiometricManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Provides testing APIs for {@link BiometricManager} */
@Implements(
    className = "android.hardware.biometrics.BiometricManager",
    minSdk = Q,
    isInAndroidSdk = false)
public class ShadowBiometricManager {

  protected boolean biometricServiceConnected = true;

  @SuppressWarnings("deprecation")
  @RequiresPermission(USE_BIOMETRIC)
  @Implementation(maxSdk = Q)
  protected int canAuthenticate() {
    if (biometricServiceConnected) {
      return BIOMETRIC_SUCCESS;
    } else {
      if (!BiometricManager.hasBiometrics(RuntimeEnvironment.application.getApplicationContext())) {
        return BIOMETRIC_ERROR_NO_HARDWARE;
      } else {
        return BIOMETRIC_ERROR_HW_UNAVAILABLE;
      }
    }
  }

  /**
   * Sets the value {@code true} to allow {@link #canAuthenticate()} return {@link
   * BIOMETRIC_SUCCESS} If sets the value to {@code false}, result will depend on {@link
   * BiometricManager#hasBiometrics(Context context)}
   *
   * @param flag to set can authenticate or not
   */
  public void setCanAuthenticate(boolean flag) {
    biometricServiceConnected = flag;
  }
}
