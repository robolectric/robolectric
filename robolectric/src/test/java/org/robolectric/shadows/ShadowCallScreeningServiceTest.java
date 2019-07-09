package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit test for {@link ShadowCallScreeningService}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.N)
public class ShadowCallScreeningServiceTest {
  private TestCallScreeningService callScreeningService;
  private ShadowCallScreeningService shadowCallScreeningService;

  @Before
  public void setUp() {
    callScreeningService = new TestCallScreeningService();
    shadowCallScreeningService = Shadow.extract(callScreeningService);
  }

  @Test
  public void getLastRespondToCallInput_whenRespondToCallNotCalled_shouldReturnEmptyOptional() {
    Optional<ShadowCallScreeningService.RespondToCallInput> lastRespondToCallInputOptional =
        shadowCallScreeningService.getLastRespondToCallInput();
    assertThat(lastRespondToCallInputOptional.isPresent()).isFalse();
  }

  @Test
  public void getLastRespondToCallInput_shouldReturnTestCallDetails() {
    // testing with null since instantiating a Call.Details object is tedious and brittle
    Call.Details testCallDetails = null;
    callScreeningService.onScreenCall(testCallDetails);

    Optional<ShadowCallScreeningService.RespondToCallInput> lastRespondToCallInputOptional =
        shadowCallScreeningService.getLastRespondToCallInput();
    assertThat(lastRespondToCallInputOptional.isPresent()).isTrue();
    ShadowCallScreeningService.RespondToCallInput respondToCallInput =
        shadowCallScreeningService.getLastRespondToCallInput().get();
    assertThat(respondToCallInput.getCallDetails()).isNull();
  }

  @Test
  public void getLastRespondToCallInput_shouldReturnTestCallResponse() {
    Call.Details testCallDetails = null;
    callScreeningService.onScreenCall(testCallDetails);

    assertThat(shadowCallScreeningService.getLastRespondToCallInput().isPresent()).isTrue();
    ShadowCallScreeningService.RespondToCallInput respondToCallInput =
        shadowCallScreeningService.getLastRespondToCallInput().get();
    assertThat(respondToCallInput.getCallResponse().getRejectCall()).isTrue();
  }

  private static class TestCallScreeningService extends CallScreeningService {
    @Override
    public void onScreenCall(Call.Details details) {
      CallResponse callResponse =
          new CallResponse.Builder()
              .setDisallowCall(true)
              .setRejectCall(true)
              .setSkipNotification(true)
              .build();

      respondToCall(details, callResponse);
    }
  }
}
