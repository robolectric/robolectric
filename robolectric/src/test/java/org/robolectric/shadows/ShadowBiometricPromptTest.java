package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.Executor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public final class ShadowBiometricPromptTest {
  private static final int ERROR_CODE = 1;
  private static final String ERROR_MESSAGE = "error";

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  private final Context context = ApplicationProvider.getApplicationContext();
  private final Executor executor = new HandlerExecutor(new Handler(Looper.getMainLooper()));
  private final BiometricPrompt biometricPrompt =
      new BiometricPrompt.Builder(context)
          .setTitle("title")
          .setSubtitle("subtitle")
          .setNegativeButton("negative", executor, (dialog, which) -> {})
          .build();
  private final CancellationSignal cancellationSignal = new CancellationSignal();

  @Mock private AuthenticationCallback authenticationCallback1;
  @Mock private AuthenticationCallback authenticationCallback2;

  @Test
  public void getCurrentPrompt_noPrompt_returnsNull() {
    assertThat(ShadowBiometricPrompt.getCurrentPrompt()).isNull();
  }

  @Test
  public void getCurrentPrompt_returnsPromptInProgress() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);

    assertThat(ShadowBiometricPrompt.getCurrentPrompt()).isEqualTo(biometricPrompt);
  }

  @Test
  public void authenticateCurrentSessionSuccessfully_triggersOnAuthenticationSucceeded() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);
    ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully();

    waitForIdle();

    verify(authenticationCallback1).onAuthenticationSucceeded(any());
  }

  @Test
  public void authenticateCurrentSessionWithError_triggersOnAuthenticationError() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);
    ShadowBiometricPrompt.authenticateCurrentSessionWithError(ERROR_CODE, ERROR_MESSAGE);

    waitForIdle();

    verify(authenticationCallback1).onAuthenticationError(eq(ERROR_CODE), eq(ERROR_MESSAGE));
  }

  @Test
  public void noAuthenticateCurrentSession_throwsIllegalStateException() {
    assertThrows(
        IllegalStateException.class,
        () -> ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully());
    assertThrows(
        IllegalStateException.class,
        () -> ShadowBiometricPrompt.authenticateCurrentSessionWithError(ERROR_CODE, ERROR_MESSAGE));
  }

  @Test
  public void
      authenticateCurrentSessionSuccessfully_thenAuthenticateWithError_throwsIllegalStateException() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);
    ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully();

    waitForIdle();

    assertThrows(
        IllegalStateException.class,
        () -> ShadowBiometricPrompt.authenticateCurrentSessionWithError(ERROR_CODE, ERROR_MESSAGE));
  }

  @Test
  public void
      authenticateCurrentSessionWithError_thenAuthenticateCurrentSessionSuccessfully_throwsIllegalStateException() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);
    ShadowBiometricPrompt.authenticateCurrentSessionWithError(ERROR_CODE, ERROR_MESSAGE);

    waitForIdle();

    assertThrows(
        IllegalStateException.class,
        () -> ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully());
  }

  @Test
  public void authenticate_multipleTimes_triggersOnAuthenticationSucceededMultipleTimes() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);
    ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully();

    waitForIdle();

    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback2);
    ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully();

    waitForIdle();

    verify(authenticationCallback1).onAuthenticationSucceeded(any());
    verify(authenticationCallback2).onAuthenticationSucceeded(any());
  }

  @Test
  public void
      cancelCurrentSession_thenAuthenticateCurrentSessionSuccessfully_triggersOnAuthenticationErrorAndthrowsIllegalStateException() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);
    ShadowBiometricPrompt.cancelCurrentSession();

    waitForIdle();

    verify(authenticationCallback1)
        .onAuthenticationError(
            eq(BiometricPrompt.BIOMETRIC_ERROR_CANCELED), eq("Authentication canceled"));
    assertThrows(
        IllegalStateException.class,
        () -> ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully());
  }

  @Test
  public void
      cancelCurrentSessionWithCancellationSignal_thenAuthenticateCurrentSessionSuccessfully_triggersOnAuthenticationErrorAndthrowsIllegalStateException() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);
    cancellationSignal.cancel();

    waitForIdle();

    verify(authenticationCallback1)
        .onAuthenticationError(
            eq(BiometricPrompt.BIOMETRIC_ERROR_CANCELED), eq("Authentication canceled"));
    assertThrows(
        IllegalStateException.class,
        () -> ShadowBiometricPrompt.authenticateCurrentSessionSuccessfully());
  }

  @Test
  public void failCurrentSessionOnce_callsOnAuthenticationFailedAndDoesNotClearSession() {
    biometricPrompt.authenticate(cancellationSignal, executor, authenticationCallback1);

    ShadowBiometricPrompt.failCurrentSessionOnce();
    waitForIdle();

    verify(authenticationCallback1).onAuthenticationFailed();
    assertThat(ShadowBiometricPrompt.getCurrentPrompt()).isNotNull();
  }

  private void waitForIdle() {
    shadowOf(Looper.getMainLooper()).idle();
  }
}
