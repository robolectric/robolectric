package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.VoiceInteractor.AbortVoiceRequest;
import android.app.VoiceInteractor.CommandRequest;
import android.app.VoiceInteractor.CompleteVoiceRequest;
import android.app.VoiceInteractor.ConfirmationRequest;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.Prompt;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowVoiceInteractor}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = TIRAMISU)
public final class ShadowVoiceInteractorTest {

  private static final String PROMPT_MESSAGE_1 = "Message_1";
  private static final String PROMPT_MESSAGE_2 = "Message_2";

  private Activity testActivity;
  private ShadowVoiceInteractor shadowVoiceInteractor;

  @Before
  public void setUp() {
    testActivity = buildActivity(Activity.class).create().get();
    shadowOf(testActivity).initializeVoiceInteractor();
    shadowVoiceInteractor = shadowOf(testActivity.getVoiceInteractor());
  }

  @Test
  public void testGetDirectActionsInvalidationCount() {
    testActivity.getVoiceInteractor().notifyDirectActionsChanged();
    assertThat(shadowVoiceInteractor.getDirectActionsInvalidationCount()).isEqualTo(1);
    AssertionError unused =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(shadowVoiceInteractor.getDirectActionsInvalidationCount())
                    .isEqualTo(2));
    testActivity.getVoiceInteractor().notifyDirectActionsChanged();
    assertThat(shadowVoiceInteractor.getDirectActionsInvalidationCount()).isEqualTo(2);
  }

  @Test
  public void voiceInteractions_completeVoiceRequest() {
    CompleteVoiceRequest completeVoiceRequest =
        new CompleteVoiceRequest(new Prompt(PROMPT_MESSAGE_1), /* extras= */ null);

    testActivity.getVoiceInteractor().submitRequest(completeVoiceRequest);

    assertValues(Collections.singletonList(PROMPT_MESSAGE_1));
  }

  @Test
  public void voiceInteractions_confirmRequest() {
    ConfirmationRequest confirmationRequest =
        new ConfirmationRequest(new Prompt(PROMPT_MESSAGE_1), /* extras= */ null);

    testActivity.getVoiceInteractor().submitRequest(confirmationRequest);

    assertValues(Collections.singletonList(PROMPT_MESSAGE_1));
  }

  @Test
  public void voiceInteractions_abortVoiceRequest() {
    AbortVoiceRequest abortVoiceRequest =
        new AbortVoiceRequest(new Prompt(PROMPT_MESSAGE_1), /* extras= */ null);

    testActivity.getVoiceInteractor().submitRequest(abortVoiceRequest);

    assertValues(Collections.singletonList(PROMPT_MESSAGE_1));
  }

  @Test
  public void voiceInteractions_commandRequest() {
    CommandRequest commandRequest = new CommandRequest(PROMPT_MESSAGE_1, /* extras= */ null);

    testActivity.getVoiceInteractor().submitRequest(commandRequest);

    assertValues(Collections.singletonList(PROMPT_MESSAGE_1));
  }

  @Test
  public void voiceInteractions_pickOptionsRequest() {
    PickOptionRequest pickOptionRequest =
        new PickOptionRequest(
            new Prompt(PROMPT_MESSAGE_1), /* options= */ null, /* extras= */ null);

    testActivity.getVoiceInteractor().submitRequest(pickOptionRequest);

    assertValues(Collections.singletonList(PROMPT_MESSAGE_1));
  }

  @Test
  public void voiceInteractions_withMultipleRequests() {
    CompleteVoiceRequest completeVoiceRequest =
        new CompleteVoiceRequest(new Prompt(PROMPT_MESSAGE_1), /* extras= */ null);
    testActivity.getVoiceInteractor().submitRequest(completeVoiceRequest);
    ConfirmationRequest confirmationRequest =
        new ConfirmationRequest(new Prompt(PROMPT_MESSAGE_2), /* extras= */ null);
    testActivity.getVoiceInteractor().submitRequest(confirmationRequest);

    assertValues(ImmutableList.of(PROMPT_MESSAGE_1, PROMPT_MESSAGE_2));
  }

  @Test
  public void voiceInteractions_returnsTrue() {
    ConfirmationRequest confirmationRequest =
        new ConfirmationRequest(new Prompt(PROMPT_MESSAGE_1), /* extras= */ null);

    assertThat(testActivity.getVoiceInteractor().submitRequest(confirmationRequest)).isTrue();
  }

  @Test
  public void getPackageName_returnsDefaultPackageName() {
    assertThat(testActivity.getVoiceInteractor().getPackageName())
        .isEqualTo(shadowVoiceInteractor.getPackageName());
  }

  @Test
  public void getPackageName_returnsModifiedPackageName() {
    shadowVoiceInteractor.setPackageName("random_voice_interactor");
    assertThat(testActivity.getVoiceInteractor().getPackageName())
        .isEqualTo("random_voice_interactor");
  }

  private void assertValues(List<String> promptMessage) {
    assertThat(shadowVoiceInteractor.getVoiceInteractions().size()).isEqualTo(promptMessage.size());
    assertThat(shadowVoiceInteractor.getVoiceInteractions()).isEqualTo(promptMessage);
  }
}
