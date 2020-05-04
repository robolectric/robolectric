package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.P;

import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Provides testing APIs for {@link FingerprintManager} */
@SuppressWarnings("NewApi")
@Implements(FingerprintManager.class)
public class ShadowFingerprintManager {

  private static final String TAG = "ShadowFingerprintManager";

  private boolean isHardwareDetected;
  protected CryptoObject pendingCryptoObject;
  private AuthenticationCallback pendingCallback;
  private List<Fingerprint> fingerprints = Collections.emptyList();

  /**
   * Simulates a successful fingerprint authentication. An authentication request must have been
   * issued with {@link FingerprintManager#authenticate(CryptoObject, CancellationSignal, int, AuthenticationCallback, Handler)} and not cancelled.
   */
  public void authenticationSucceeds() {
    if (pendingCallback == null) {
      throw new IllegalStateException("No active fingerprint authentication request.");
    }

    pendingCallback.onAuthenticationSucceeded(createAuthenticationResult());
  }

  protected AuthenticationResult createAuthenticationResult() {
    if (RuntimeEnvironment.getApiLevel() >= N_MR1) {
      return new AuthenticationResult(pendingCryptoObject, null, 0);
    } else {
      return ReflectionHelpers.callConstructor(
          AuthenticationResult.class,
          ClassParameter.from(CryptoObject.class, pendingCryptoObject),
          ClassParameter.from(Fingerprint.class, null));
    }
  }

  /**
   * Simulates a failed fingerprint authentication. An authentication request must have been
   * issued with {@link FingerprintManager#authenticate(CryptoObject, CancellationSignal, int, AuthenticationCallback, Handler)} and not cancelled.
   */
  public void authenticationFails() {
    if (pendingCallback == null) {
      throw new IllegalStateException("No active fingerprint authentication request.");
    }

    pendingCallback.onAuthenticationFailed();
  }

  /**
   * Success or failure can be simulated with a subsequent call to {@link #authenticationSucceeds()}
   * or {@link #authenticationFails()}.
   */
  @Implementation(minSdk = M)
  protected void authenticate(
      CryptoObject crypto,
      CancellationSignal cancel,
      int flags,
      AuthenticationCallback callback,
      Handler handler) {
    if (callback == null) {
      throw new IllegalArgumentException("Must supply an authentication callback");
    }

    if (cancel != null) {
      if (cancel.isCanceled()) {
        Log.w(TAG, "authentication already canceled");
        return;
      } else {
        cancel.setOnCancelListener(() -> {
          this.pendingCallback = null;
          this.pendingCryptoObject = null;
        });
      }
    }

    this.pendingCryptoObject = crypto;
    this.pendingCallback = callback;
  }

  /**
   * Sets the return value of {@link FingerprintManager#hasEnrolledFingerprints()}.
   *
   * @deprecated use {@link #setDefaultFingerprints} instead.
   */
  @Deprecated
  public void setHasEnrolledFingerprints(boolean hasEnrolledFingerprints) {
    setDefaultFingerprints(hasEnrolledFingerprints ? 1 : 0);
  }

  /**
   * Returns {@code false} by default, or the value specified via
   * {@link #setHasEnrolledFingerprints(boolean)}.
   */
  @Implementation(minSdk = M)
  protected boolean hasEnrolledFingerprints() {
    return !fingerprints.isEmpty();
  }

  /**
   * @return lists of current fingerprint items, the list be set via {@link #setDefaultFingerprints}
   */
  @HiddenApi
  @Implementation(minSdk = M)
  protected List<Fingerprint> getEnrolledFingerprints() {
    return new ArrayList<>(fingerprints);
  }

  /**
   * @return Returns the finger ID for the given index.
   */
  public int getFingerprintId(int index) {
    return ReflectionHelpers.callInstanceMethod(
        getEnrolledFingerprints().get(index),
        RuntimeEnvironment.getApiLevel() > P ? "getBiometricId" : "getFingerId");
  }

  /**
   * Enrolls the given number of fingerprints, which will be returned in {@link
   * #getEnrolledFingerprints}.
   *
   * @param num the quantity of fingerprint item.
   */
  public void setDefaultFingerprints(int num) {
    setEnrolledFingerprints(
        IntStream.range(0, num)
            .mapToObj(
                i ->
                    new Fingerprint(
                        /* name= */ "Fingerprint " + i,
                        /* groupId= */ 0,
                        /* fingerId= */ i,
                        /* deviceId= */ 0))
            .toArray(Fingerprint[]::new));
  }

  private void setEnrolledFingerprints(Fingerprint... fingerprints) {
    this.fingerprints = Arrays.asList(fingerprints);
  }

  /**
   * Sets the return value of {@link FingerprintManager#isHardwareDetected()}.
   */
  public void setIsHardwareDetected(boolean isHardwareDetected) {
    this.isHardwareDetected = isHardwareDetected;
  }

  /**
   * @return `false` by default, or the value specified via {@link #setIsHardwareDetected(boolean)}
   */
  @Implementation(minSdk = M)
  protected boolean isHardwareDetected() {
    return this.isHardwareDetected;
  }
}
