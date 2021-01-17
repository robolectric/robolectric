package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.Q;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowVoiceInteractionSession}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Q)
public class ShadowVoiceInteractionSessionTest {

  private VoiceInteractionSession session;
  private ShadowVoiceInteractionSession shadowSession;

  @Before
  public void setUp() {
    session = new VoiceInteractionSession(getApplicationContext());
    shadowSession = Shadow.extract(session);
  }

  @Test
  public void isWindowShowing_returnsFalseByDefault() {
    shadowSession.create();

    assertThat(shadowSession.isWindowShowing()).isFalse();
  }

  @Test
  public void isWindowShowing_afterShow_returnsTrue() {
    shadowSession.create();

    session.show(new Bundle(), /* flags= */ 0);

    assertThat(shadowSession.isWindowShowing()).isTrue();
  }

  @Test
  public void isWindowShowing_afterShowThenHide_returnsFalse() {
    shadowSession.create();

    session.show(new Bundle(), /* flags= */ 0);
    session.hide();

    assertThat(shadowSession.isWindowShowing()).isFalse();
  }

  @Test
  public void startAssistantActivity_invokedTwice_lastIntentRegistered() {
    shadowSession.create();
    Intent intent1 = new Intent("foo action");
    Intent intent2 = new Intent("bar action");

    session.startAssistantActivity(intent1);
    session.startAssistantActivity(intent2);

    assertThat(shadowSession.getLastAssistantActivityIntent()).isEqualTo(intent2);
  }

  @Test
  public void startAssistantActivity_invokedTwice_allIntentsRegisteredInOrder() {
    shadowSession.create();
    Intent intent1 = new Intent("foo action");
    Intent intent2 = new Intent("bar action");

    session.startAssistantActivity(intent1);
    session.startAssistantActivity(intent2);

    assertThat(shadowSession.getAssistantActivityIntents())
        .containsExactly(intent1, intent2)
        .inOrder();
  }

  @Test
  public void startAssistantActivity_notInvoked_noRegisteredIntents() {
    assertThat(shadowSession.getAssistantActivityIntents()).isEmpty();
  }

  @Test
  public void startAssistantActivity_notInvoked_lastRegisteredIntentIsNull() {
    assertThat(shadowSession.getLastAssistantActivityIntent()).isNull();
  }

  @Test(expected = SecurityException.class)
  public void startVoiceActivity_exceptionSet_throws() {
    shadowSession.create();

    shadowSession.setStartVoiceActivityException(new SecurityException());

    session.startVoiceActivity(new Intent());
  }

  @Test
  public void startVoiceActivity_invokedTwice_lastIntentRegistered() {
    shadowSession.create();
    Intent intent1 = new Intent("foo action");
    Intent intent2 = new Intent("bar action");

    session.startVoiceActivity(intent1);
    session.startVoiceActivity(intent2);

    assertThat(shadowSession.getLastVoiceActivityIntent()).isEqualTo(intent2);
  }

  @Test
  public void startVoiceActivity_invokedTwice_allIntentsRegisteredInOrder() {
    shadowSession.create();
    Intent intent1 = new Intent("foo action");
    Intent intent2 = new Intent("bar action");

    session.startVoiceActivity(intent1);
    session.startVoiceActivity(intent2);

    assertThat(shadowSession.getVoiceActivityIntents()).containsExactly(intent1, intent2).inOrder();
  }

  @Test
  public void startVoiceActivity_notInvoked_noRegisteredIntents() {
    assertThat(shadowSession.getVoiceActivityIntents()).isEmpty();
  }

  @Test
  public void startVoiceActivity_notInvoked_lastRegisteredIntentIsNull() {
    assertThat(shadowSession.getVoiceActivityIntents()).isEmpty();
  }

  @Test
  public void isUiEnabled_returnsTrueByDefault() {
    assertThat(shadowSession.isUiEnabled()).isTrue();
  }

  @Test
  public void isUiEnabled_afterSettingDisabled_returnsFalse() {
    session.setUiEnabled(false);

    assertThat(shadowSession.isUiEnabled()).isFalse();
  }

  @Test
  public void isUiEnabled_afterSettingDisabledThenEnabled_returnsTrue() {
    session.setUiEnabled(false);
    session.setUiEnabled(true);

    assertThat(shadowSession.isUiEnabled()).isTrue();
  }

  @Test(expected = RuntimeException.class)
  @Config(sdk = N)
  public void isUiEnabled_belowAndroidO_throws() {
    shadowSession.isUiEnabled();
  }
}
