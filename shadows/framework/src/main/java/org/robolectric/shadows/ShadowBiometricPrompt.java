package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.CallbackExecutor;
import android.annotation.NonNull;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback;
import android.hardware.biometrics.BiometricPrompt.AuthenticationResult;
import android.hardware.biometrics.BiometricPrompt.CryptoObject;
import android.os.Build;
import android.os.CancellationSignal;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

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

  @Implementation
  protected void authenticate(
      CancellationSignal cancel,
      @CallbackExecutor Executor executor,
      AuthenticationCallback callback) {
    reflector(BiometricPromptReflector.class, realBiometricPrompt)
        .authenticate(cancel, executor, callback);
    synchronized (lock) {
      currentAuthenticateSession = new AuthenticateSession(callback, cancel, executor);
    }
  }

  @Implementation
  protected void authenticate(
      CryptoObject crypto,
      CancellationSignal cancel,
      @CallbackExecutor Executor executor,
      AuthenticationCallback callback) {
    reflector(BiometricPromptReflector.class, realBiometricPrompt)
        .authenticate(crypto, cancel, executor, callback);
    synchronized (lock) {
      currentAuthenticateSession = new AuthenticateSession(callback, cancel, executor);
    }
  }

  private static AuthenticateSession requireCurrentSession() {
    AuthenticateSession session;
    synchronized (lock) {
      checkState(
          currentAuthenticateSession != null,
          "No current authentication session. Did you forget to call authenticate() first?");
      session = currentAuthenticateSession;
    }
    return session;
  }

  private static AuthenticateSession requireAndClearCurrentSession() {
    AuthenticateSession authenticateSession;
    synchronized (lock) {
      checkState(
          currentAuthenticateSession != null,
          "No current authentication session. Did you forget to call authenticate() first?");
      authenticateSession = currentAuthenticateSession;
      currentAuthenticateSession = null;
    }
    return authenticateSession;
  }

  private class AuthenticateSession {
    private final AuthenticationCallback callback;
    private final CancellationSignal cancel;
    private final Executor executor;

    AuthenticateSession(
        AuthenticationCallback callback,
        CancellationSignal cancel,
        @CallbackExecutor Executor executor) {
      requireNonNull(callback, "AuthenticationCallback must be non-null");
      requireNonNull(cancel, "CancellationSignal must be non-null");
      requireNonNull(executor, "Executor must be non-null");

      this.callback = callback;
      this.cancel = cancel;
      this.executor = executor;

      cancel.setOnCancelListener(
          () -> {
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

    private void cancelSession() {
      cancel.cancel();
    }

    private void authenticateSuccessfully() {
      executor.execute(() -> callback.onAuthenticationSucceeded(getAuthenticationResult()));
    }

    private void authenticateWithError(int errorCode, String message) {
      executor.execute(() -> callback.onAuthenticationError(errorCode, message));
    }

    private AuthenticationResult getAuthenticationResult() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return ReflectionHelpers.callConstructor(
            AuthenticationResult.class,
            ClassParameter.from(CryptoObject.class, null),
            ClassParameter.from(int.class, 0));
      } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        return ReflectionHelpers.callConstructor(
            AuthenticationResult.class, ClassParameter.from(CryptoObject.class, null));
      } else {
        try {
          return ReflectionHelpers.callConstructor(
              AuthenticationResult.class,
              ClassParameter.from(CryptoObject.class, null),
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

  @ForType(BiometricPrompt.class)
  interface BiometricPromptReflector {
    @Direct
    void authenticate(
        @NonNull CancellationSignal cancel,
        @NonNull @CallbackExecutor Executor executor,
        @NonNull AuthenticationCallback callback);

    @Direct
    void authenticate(
        @NonNull CryptoObject crypto,
        @NonNull CancellationSignal cancel,
        @NonNull @CallbackExecutor Executor executor,
        @NonNull AuthenticationCallback callback);
  }
}
