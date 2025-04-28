package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.telecom.CallRedirectionService;
import android.telecom.PhoneAccountHandle;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowCallRedirectionService.RedirectionResult;
import org.robolectric.shadows.ShadowCallRedirectionService.RedirectionResult.RedirectCallArgs;
import org.robolectric.shadows.ShadowCallRedirectionService.RedirectionResult.RedirectionResultType;

/** Unit test for {@link ShadowCallRedirectionService}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.Q)
public class ShadowCallRedirectionServiceTest {

  private static final PhoneAccountHandle PHONE_ACCOUNT_HANDLE =
      new PhoneAccountHandle(
          new ComponentName(ApplicationProvider.getApplicationContext(), Object.class), "handle");
  private static final Uri REDIRECT_URI = Uri.parse("tel:1234567890");
  private TestCallRedirectionService callRedirectionService;

  @Before
  public void setUp() {
    callRedirectionService = Robolectric.setupService(TestCallRedirectionService.class);
  }

  @Test
  public void placeCall_withCallUnmodified_capturesPlaceCallUnmodifiedResult() {
    ShadowCallRedirectionService shadowService = Shadow.extract(callRedirectionService);
    callRedirectionService.setResultType(RedirectionResultType.PLACE_CALL_UNMODIFIED);

    shadowService.placeCall(Uri.EMPTY, PHONE_ACCOUNT_HANDLE, true);
    ShadowLooper.idleMainLooper();

    Optional<RedirectionResult> result = shadowService.getRedirectionResult();
    assertThat(result.map(RedirectionResult::getRedirectionResultType))
        .hasValue(RedirectionResultType.PLACE_CALL_UNMODIFIED);
    assertThat(result.flatMap(RedirectionResult::getRedirectCallArgs)).isEmpty();
  }

  @Test
  public void placeCall_withCallCancelled_capturesCancelCallResult() {
    ShadowCallRedirectionService shadowService = Shadow.extract(callRedirectionService);
    callRedirectionService.setResultType(RedirectionResultType.CANCEL_CALL);

    shadowService.placeCall(Uri.EMPTY, PHONE_ACCOUNT_HANDLE, true);
    ShadowLooper.idleMainLooper();

    Optional<RedirectionResult> result = shadowService.getRedirectionResult();
    assertThat(result.map(RedirectionResult::getRedirectionResultType))
        .hasValue(RedirectionResultType.CANCEL_CALL);
    assertThat(result.flatMap(RedirectionResult::getRedirectCallArgs)).isEmpty();
  }

  @Test
  public void placeCall_withCallRedirected_capturesRedirectCallResult() {
    ShadowCallRedirectionService shadowService = Shadow.extract(callRedirectionService);
    callRedirectionService.setResultType(RedirectionResultType.REDIRECT_CALL);

    shadowService.placeCall(Uri.EMPTY, PHONE_ACCOUNT_HANDLE, true);
    ShadowLooper.idleMainLooper();

    Optional<RedirectionResult> result = shadowService.getRedirectionResult();
    assertThat(result.map(RedirectionResult::getRedirectionResultType))
        .hasValue(RedirectionResultType.REDIRECT_CALL);
    Optional<RedirectCallArgs> redirectCallArgs =
        result.flatMap(RedirectionResult::getRedirectCallArgs);
    assertThat(redirectCallArgs).isPresent();
    assertThat(redirectCallArgs.get().getHandle()).isEqualTo(REDIRECT_URI);
    assertThat(redirectCallArgs.get().getTargetPhoneAccount()).isEqualTo(PHONE_ACCOUNT_HANDLE);
    assertThat(redirectCallArgs.get().getConfirmFirst()).isTrue();
  }

  private static class TestCallRedirectionService extends CallRedirectionService {
    private RedirectionResultType resultType = RedirectionResultType.PLACE_CALL_UNMODIFIED;

    private void setResultType(RedirectionResultType resultType) {
      this.resultType = resultType;
    }

    @Override
    public void onPlaceCall(
        @NonNull Uri handle,
        @NonNull PhoneAccountHandle initialPhoneAccount,
        boolean allowInteractiveResponse) {
      switch (resultType) {
        case PLACE_CALL_UNMODIFIED:
          placeCallUnmodified();
          break;
        case CANCEL_CALL:
          cancelCall();
          break;
        case REDIRECT_CALL:
          redirectCall(REDIRECT_URI, initialPhoneAccount, true);
          break;
      }
    }
  }
}
