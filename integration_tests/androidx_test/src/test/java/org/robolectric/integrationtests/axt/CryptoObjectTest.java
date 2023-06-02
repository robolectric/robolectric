package org.robolectric.integrationtests.axt;

import static org.junit.Assert.fail;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.PromptInfo;
import androidx.fragment.app.FragmentActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;

/** Test intercepting classes not present in OpenJDK. */
@RunWith(AndroidJUnit4.class)
public class CryptoObjectTest {

  private FragmentActivity fragmentActivity;

  @Before
  public void setUp() {
    fragmentActivity =
        ActivityController.of(new FragmentActivity()).create().resume().start().get();
  }

  @Test
  public void biometricPromptAuthenticateShouldNotCrashWithNoSuchMethodError()
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    BiometricPrompt biometricPrompt =
        new BiometricPrompt(
            fragmentActivity,
            new Executor() {
              @Override
              public void execute(Runnable command) {}
            },
            new BiometricPrompt.AuthenticationCallback() {
              @Override
              public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {}

              @Override
              public void onAuthenticationSucceeded(
                  @NonNull BiometricPrompt.AuthenticationResult result) {}

              @Override
              public void onAuthenticationFailed() {}
            });

    PromptInfo promptInfo =
        new PromptInfo.Builder()
            .setTitle("Set and not empty")
            .setNegativeButtonText("Set and not empty")
            .build();
    try {
      biometricPrompt.authenticate(
          promptInfo, new BiometricPrompt.CryptoObject(Cipher.getInstance("RSA")));
    } catch (NoSuchMethodError e) {
      fail();
    }
  }
}
