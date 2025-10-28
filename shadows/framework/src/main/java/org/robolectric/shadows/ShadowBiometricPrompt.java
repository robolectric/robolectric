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

  @Implementation
  protected void authenticate(
      CancellationSignal cancel,
      @CallbackExecutor Executor executor,
      AuthenticationCallback callback) {
    reflector(BiometricPromptReflector.class, realBiometricPrompt)
        .authenticate(cancel, executor, callback);
    synchronized (lock) {
      currentAuthenticateSession =
          new AuthenticateSession(realBiometricPrompt, callback, cancel, executor);
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
      currentAuthenticateSession =
          new AuthenticateSession(realBiometricPrompt, callback, cancel, executor);
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
    private final AuthenticationCallback callback;
    private final CancellationSignal cancel;
    private final Executor executor;

    AuthenticateSession(
        BiometricPrompt prompt,
        AuthenticationCallback callback,
        CancellationSignal cancel,
        @CallbackExecutor Executor executor) {
      requireNonNull(prompt, "BiometricPrompt must be non-null");
      requireNonNull(callback, "AuthenticationCallback must be non-null");
      requireNonNull(cancel, "CancellationSignal must be non-null");
      requireNonNull(executor, "Executor must be non-null");

      this.prompt = prompt;
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

    private BiometricPrompt getPrompt() {
      return prompt;
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

    private void failOnce() {
      executor.execute(() -> callback.onAuthenticationFailed());
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
