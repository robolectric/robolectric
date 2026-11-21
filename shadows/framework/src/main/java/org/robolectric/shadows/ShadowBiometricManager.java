package org.robolectric.shadows;

import static android.Manifest.permission.USE_BIOMETRIC;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresPermission;
import android.content.Context;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricManager.Authenticators;
import android.os.UserHandle;
import java.security.SecureRandom;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Provides testing APIs for {@link BiometricManager} */
@Implements(
    className = "android.hardware.biometrics.BiometricManager",
    minSdk = Q,
    isInAndroidSdk = false)
public class ShadowBiometricManager {

  protected static boolean biometricServiceConnected = true;
  private static int authenticatorType = BiometricManager.Authenticators.EMPTY_SET;
  private long lastAuthenticationTime = 0L;
  // The secure id of the enrolled strong biometrics, which keys that are invalidated by biometric
  // enrollment are bound to. It changes when the biometrics are removed.
  private static long biometricSecureId;

  @RealObject private BiometricManager realBiometricManager;

  @Resetter
  public static void reset() {
    biometricServiceConnected = true;
    authenticatorType = BiometricManager.Authenticators.EMPTY_SET;
    biometricSecureId = 0;
  }

  /**
   * Returns the secure ids of the enrolled strong biometrics, which keys requiring biometric
   * authentication are bound to.
   */
  @Implementation(minSdk = S)
  protected long[] getAuthenticatorIds(int userId) {
    long secureId = getBiometricSecureId();
    return secureId == 0 ? new long[0] : new long[] {secureId};
  }

  /**
   * Returns the secure id of the enrolled strong biometrics, or 0 if there are none. Like on a
   * device, biometrics can only be enrolled with a secure lock screen.
   */
  static long getBiometricSecureId() {
    if (!hasStrongBiometrics()
        || ShadowKeyguardManager.getSecureUserId(UserHandle.myUserId()) == 0) {
      biometricSecureId = 0;
      return 0;
    }
    while (biometricSecureId == 0) {
      biometricSecureId = new SecureRandom().nextLong();
    }
    return biometricSecureId;
  }

  private static boolean hasStrongBiometrics() {
    return biometricServiceConnected
        && (authenticatorType == Authenticators.EMPTY_SET
            || (authenticatorType & ~Authenticators.DEVICE_CREDENTIAL)
                == Authenticators.BIOMETRIC_STRONG);
  }

  private static void onBiometricsChanged() {
    if (!hasStrongBiometrics()) {
      // Removing all biometrics invalidates the keys that are bound to them.
      biometricSecureId = 0;
    }
  }

  @RequiresPermission(USE_BIOMETRIC)
  @Implementation
  protected int canAuthenticate() {
    if (RuntimeEnvironment.getApiLevel() >= R) {
      return reflector(BiometricManagerReflector.class, realBiometricManager).canAuthenticate();
    } else {
      int biometricResult =
          canAuthenticateInternal(0, BiometricManager.Authenticators.BIOMETRIC_WEAK);
      if (biometricServiceConnected) {
        return BiometricManager.BIOMETRIC_SUCCESS;
      } else if (biometricResult != BiometricManager.BIOMETRIC_SUCCESS) {
        return biometricResult;
      } else {
        boolean hasBiometrics =
            ReflectionHelpers.callStaticMethod(
                BiometricManager.class,
                "hasBiometrics",
                ReflectionHelpers.ClassParameter.from(
                    Context.class, RuntimeEnvironment.getApplication().getApplicationContext()));
        if (!hasBiometrics) {
          return BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
        } else {
          return BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE;
        }
      }
    }
  }

  @RequiresPermission(USE_BIOMETRIC)
  @Implementation(minSdk = R)
  protected int canAuthenticate(int authenticators) {
    return canAuthenticateInternal(0, authenticators);
  }

  @RequiresPermission(USE_BIOMETRIC)
  @Implementation(minSdk = R)
  protected int canAuthenticate(int userId, int authenticators) {
    return canAuthenticateInternal(userId, authenticators);
  }

  private int canAuthenticateInternal(int userId, int authenticators) {
    if (authenticatorType == BiometricManager.Authenticators.BIOMETRIC_STRONG
        && biometricServiceConnected) {
      return BiometricManager.BIOMETRIC_SUCCESS;
    }
    if ((authenticatorType & BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        == BiometricManager.Authenticators.DEVICE_CREDENTIAL) {
      return BiometricManager.BIOMETRIC_SUCCESS;
    }
    if (authenticatorType != BiometricManager.Authenticators.EMPTY_SET) {
      return authenticatorType;
    }
    if (!biometricServiceConnected) {
      return BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
    } else {
      return BiometricManager.BIOMETRIC_SUCCESS;
    }
  }

  /**
   * Sets the value {@code true} to allow {@link #canAuthenticate()} return {@link
   * BiometricManager#BIOMETRIC_SUCCESS} If sets the value to {@code false}, result will depend on
   * {@code BiometricManager.hasBiometrics(Context)}
   *
   * @param flag to set can authenticate or not
   */
  public void setCanAuthenticate(boolean flag) {
    biometricServiceConnected = flag;
    onBiometricsChanged();
  }

  /**
   * Allow different result {@link #canAuthenticate(int)}, result will depend on the combination as
   * described <a
   * href="https://developer.android.com/reference/android/hardware/biometrics/BiometricManager#canAuthenticate(int)">here</a>
   * For example, you can set the value {@code BiometricManager.Authenticators.BIOMETRIC_STRONG} to
   * allow {@link #canAuthenticate(int)} return {@link BiometricManager#BIOMETRIC_SUCCESS} when you
   * passed {@code BiometricManager.Authenticators.BIOMETRIC_WEAK} as parameter in {@link
   * #canAuthenticate(int)}
   *
   * @param type to set the authenticatorType
   * @see <a
   *     href="https://developer.android.com/reference/android/hardware/biometrics/BiometricManager#canAuthenticate(int)">BiometricManager#canAuthenticate(int)</a>
   */
  public void setAuthenticatorType(int type) {
    authenticatorType = type;
    onBiometricsChanged();
  }

  /**
   * Sets the last authentication time.
   *
   * @param time the last authentication time or {@link
   *     BiometricManager#BIOMETRIC_NO_AUTHENTICATION}
   */
  public void setLastAuthenticationTime(long time) {
    lastAuthenticationTime = time;
  }

  /** Gets the last authentication time set by {@link #setLastAuthenticationTime(long)}. */
  @Implementation(minSdk = VANILLA_ICE_CREAM)
  protected long getLastAuthenticationTime(int authenticators) {
    return lastAuthenticationTime;
  }

  @ForType(BiometricManager.class)
  interface BiometricManagerReflector {

    @Direct
    int canAuthenticate();
  }
}
