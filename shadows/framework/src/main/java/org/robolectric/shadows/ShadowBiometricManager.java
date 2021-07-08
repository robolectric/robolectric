package org.robolectric.shadows;

import static android.Manifest.permission.USE_BIOMETRIC;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
import static android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS;
import static android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresPermission;
import android.content.Context;
import android.hardware.biometrics.BiometricManager;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Provides testing APIs for {@link BiometricManager} */
@Implements(
    className = "android.hardware.biometrics.BiometricManager",
    minSdk = Q,
    isInAndroidSdk = false)
public class ShadowBiometricManager {

  protected boolean biometricServiceConnected = true;

  @RealObject private BiometricManager realBiometricManager;

  @SuppressWarnings("deprecation")
  @RequiresPermission(USE_BIOMETRIC)
  @Implementation(maxSdk = Q)
  protected int canAuthenticate() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      return reflector(BiometricManagerReflector.class, realBiometricManager).canAuthenticate();
    } else if (biometricServiceConnected) {
      return BIOMETRIC_SUCCESS;
    } else {
      boolean hasBiomatrics =
          ReflectionHelpers.callStaticMethod(
              BiometricManager.class,
              "hasBiometrics",
              ClassParameter.from(
                  Context.class, RuntimeEnvironment.getApplication().getApplicationContext()));
      if (!hasBiomatrics) {
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

  @Implementation(minSdk = R)
  protected int canAuthenticate(int userId, int authenticators) {
    return biometricServiceConnected ? BIOMETRIC_SUCCESS : BIOMETRIC_ERROR_NO_HARDWARE;
  }

  @ForType(BiometricManager.class)
  interface BiometricManagerReflector {

    @Direct
    int canAuthenticate();
  }
}
