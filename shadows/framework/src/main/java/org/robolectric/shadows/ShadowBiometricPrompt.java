package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import android.annotation.CallbackExecutor;
import android.hardware.biometrics.BiometricManager.Authenticators;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback;
import android.hardware.biometrics.BiometricPrompt.AuthenticationResult;
import android.hardware.biometrics.BiometricPrompt.CryptoObject;
import android.hardware.security.keymint.HardwareAuthenticatorType;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.UserHandle;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Provides testing APIs for {@link BiometricPrompt} */
@Implements(
    className = "android.hardware.biometrics.BiometricPrompt",
    minSdk = P,
    isInAndroidSdk = false)
public class ShadowBiometricPrompt {
  private static final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private static AuthenticateSession currentAuthenticateSession;

  @RealObject private BiometricPrompt realBiometricPrompt;

  @Resetter
  public static void reset() {
    synchronized (lock) {
      currentAuthenticateSession = null;
    }
  }

  /**
   * Returns the {@link BiometricPrompt} for the current authentication session, or {@code null} if
   * there is no session in progress.
   *
   * <p>Use this method to check if an authentication session is in progress, and/or to perform
   * additional assertions on the {@link BiometricPrompt} being shown such as its title or
   * description.
   */
  @Nullable
  public static BiometricPrompt getCurrentPrompt() {
    AuthenticateSession session = getCurrentSession();
    if (session == null) {
      return null;
    }
    return session.getPrompt();
  }

  /** Cancels the current authentication session and triggers {@link CancellationSignal#cancel}. */
  public static void cancelCurrentSession() {
    requireCurrentSession().cancelSession();
  }

  /**
   * Simulates a successful authentication and triggers {@link
   * AuthenticationCallback#onAuthenticationSucceeded}.
   */
  public static void authenticateCurrentSessionSuccessfully() {
    requireAndClearCurrentSession().authenticateSuccessfully();
  }

  /**
   * Simulates an error authentication and triggers {@link
   * AuthenticationCallback#onAuthenticationError}.
   *
   * @param errorCode The error code to be returned.
   * @param message The error message to be returned.
   */
  public static void authenticateCurrentSessionWithError(int errorCode, String message) {
    requireAndClearCurrentSession().authenticateWithError(errorCode, message);
  }

  /**
   * Simulates a failed authentication by triggering {@link
   * AuthenticationCallback#onAuthenticationFailed}.
   *
   * <p>Note that this does not clear the current session. A failed authentication typically means
   * the user can retry.
   */
  public static void failCurrentSessionOnce() {
    requireCurrentSession().failOnce();
  }

  @Filter(order = Order.AFTER)
  protected void authenticate(
      CancellationSignal cancel,
      @CallbackExecutor Executor executor,
      AuthenticationCallback callback) {
    synchronized (lock) {
      currentAuthenticateSession =
          new AuthenticateSession(realBiometricPrompt, null, callback, cancel, executor);
    }
  }

  @Filter(order = Order.AFTER)
  protected void authenticate(
      CryptoObject crypto,
      CancellationSignal cancel,
      @CallbackExecutor Executor executor,
      AuthenticationCallback callback) {
    synchronized (lock) {
      currentAuthenticateSession =
          new AuthenticateSession(realBiometricPrompt, crypto, callback, cancel, executor);
    }
  }

  @Nullable
  private static AuthenticateSession getCurrentSession() {
    synchronized (lock) {
      return currentAuthenticateSession;
    }
  }

  private static AuthenticateSession requireCurrentSession() {
    AuthenticateSession session = getCurrentSession();
    checkState(
        session != null,
        "No current authentication session. Did you forget to call authenticate() first?");
    return session;
  }

  private static AuthenticateSession requireAndClearCurrentSession() {
    synchronized (lock) {
      checkState(
          currentAuthenticateSession != null,
          "No current authentication session. Did you forget to call authenticate() first?");
      AuthenticateSession authenticateSession = currentAuthenticateSession;
      currentAuthenticateSession = null;
      return authenticateSession;
    }
  }

  private static final class AuthenticateSession {
    private final BiometricPrompt prompt;
    @Nullable private final CryptoObject crypto;
    private final AuthenticationCallback callback;
    private final CancellationSignal cancel;
    private final Executor executor;

    AuthenticateSession(
        BiometricPrompt prompt,
        @Nullable CryptoObject crypto,
        AuthenticationCallback callback,
        CancellationSignal cancel,
        @CallbackExecutor Executor executor) {
      requireNonNull(prompt, "BiometricPrompt must be non-null");
      requireNonNull(callback, "AuthenticationCallback must be non-null");
      requireNonNull(cancel, "CancellationSignal must be non-null");
      requireNonNull(executor, "Executor must be non-null");

      this.prompt = prompt;
      this.crypto = crypto;
      this.callback = callback;
      this.cancel = cancel;
      this.executor = executor;

      cancel.setOnCancelListener(() -> {
        boolean isCurrentSession = false;
        synchronized (lock) {
          isCurrentSession = this.equals(currentAuthenticateSession);
          if (isCurrentSession) {
            currentAuthenticateSession = null;
          }
        }
        if (isCurrentSession) {
          this.authenticateWithError(
              BiometricPrompt.BIOMETRIC_ERROR_CANCELED, "Authentication canceled");
        }
      });
    }

    private BiometricPrompt getPrompt() {
      return prompt;
    }

    private void cancelSession() {
      cancel.cancel();
    }

    private void authenticateSuccessfully() {
      boolean biometric = usesBiometric();
      if (RuntimeEnvironment.getApiLevel() >= S) {
        addAuthToken(biometric);
      }
      AuthenticationResult result = getAuthenticationResult(biometric);
      executor.execute(() -> callback.onAuthenticationSucceeded(result));
    }

    /**
     * Returns whether the user authenticated with a biometric rather than their device credential,
     * which they do if the prompt allows a biometric and one is enrolled.
     */
    private boolean usesBiometric() {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        return true;
      }
      int allowed = prompt.getAllowedAuthenticators();
      boolean allowsBiometric = allowed == 0 || (allowed & ~Authenticators.DEVICE_CREDENTIAL) != 0;
      boolean allowsCredential = (allowed & Authenticators.DEVICE_CREDENTIAL) != 0;
      return allowsBiometric
          && (ShadowBiometricManager.getBiometricSecureId() != 0 || !allowsCredential);
    }

    /** Authorizes the keystore keys that this authentication unlocks, as the system does. */
    private void addAuthToken(boolean biometric) {
      long secureUserId = ShadowKeyguardManager.getSecureUserId(UserHandle.myUserId());
      long biometricSecureId = biometric ? ShadowBiometricManager.getBiometricSecureId() : 0;
      if (secureUserId == 0 && biometricSecureId == 0) {
        return;
      }
      int type =
          biometric ? HardwareAuthenticatorType.FINGERPRINT : HardwareAuthenticatorType.PASSWORD;
      if (crypto != null) {
        KeystoreServiceDelegate.addAuthTokenForLatestOperation(
            secureUserId, biometricSecureId, type);
      } else {
        KeystoreServiceDelegate.addAuthToken(0, secureUserId, biometricSecureId, type);
      }
    }

    private void authenticateWithError(int errorCode, String message) {
      executor.execute(() -> callback.onAuthenticationError(errorCode, message));
    }

    private void failOnce() {
      executor.execute(() -> callback.onAuthenticationFailed());
    }

    private AuthenticationResult getAuthenticationResult(boolean biometric) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return ReflectionHelpers.callConstructor(
            AuthenticationResult.class,
            ClassParameter.from(CryptoObject.class, crypto),
            ClassParameter.from(
                int.class,
                biometric
                    ? BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC
                    : BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL));
      } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        return ReflectionHelpers.callConstructor(
            AuthenticationResult.class, ClassParameter.from(CryptoObject.class, crypto));
      } else {
        try {
          return ReflectionHelpers.callConstructor(
              AuthenticationResult.class,
              ClassParameter.from(CryptoObject.class, crypto),
              ClassParameter.from(
                  Class.forName(
                      "android.hardware.biometrics.BiometricAuthenticator$BiometricIdentifier"),
                  null),
              ClassParameter.from(int.class, 0));
        } catch (ClassNotFoundException e) {
          throw new IllegalStateException("Failed to load BiometricIdentifier class", e);
        }
      }
    }
  }
}
