package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowTextToSpeechTest {
  private TextToSpeech textToSpeech;
  private Activity activity;
  private TextToSpeech.OnInitListener listener;

  @Before
  public void setUp() throws Exception {
    activity = Robolectric.buildActivity(Activity.class).create().get();
    listener = new TextToSpeech.OnInitListener() {
      @Override public void onInit(int i) {
      }
    };

    textToSpeech = new TextToSpeech(activity, listener);
  }

  @Test
  public void shouldNotBeNull() throws Exception {
    assertThat(textToSpeech).isNotNull();
    assertThat(shadowOf(textToSpeech)).isNotNull();
  }

  @Test
  public void getContext_shouldReturnContext() throws Exception {
    assertThat(shadowOf(textToSpeech).getContext()).isEqualTo(activity);
  }

  @Test
  public void getOnInitListener_shouldReturnListener() throws Exception {
    assertThat(shadowOf(textToSpeech).getOnInitListener()).isEqualTo(listener);
  }

  @Test
  public void getLastSpokenText_shouldReturnSpokenText() throws Exception {
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isEqualTo("Hello");
  }

  @Test
  public void getLastSpokenText_shouldReturnMostRecentText() throws Exception {
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    textToSpeech.speak("Hi", TextToSpeech.QUEUE_FLUSH, null);
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isEqualTo("Hi");
  }

  @Test
  public void clearLastSpokenText_shouldSetLastSpokenTextToNull() throws Exception {
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    shadowOf(textToSpeech).clearLastSpokenText();
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isNull();
  }

  @Test
  public void isShutdown_shouldReturnFalseBeforeShutdown() throws Exception {
    assertThat(shadowOf(textToSpeech).isShutdown()).isFalse();
  }

  @Test
  public void isShutdown_shouldReturnTrueAfterShutdown() throws Exception {
    textToSpeech.shutdown();
    assertThat(shadowOf(textToSpeech).isShutdown()).isTrue();
  }

  @Test
  public void getQueueMode_shouldReturnMostRecentQueueMode() throws Exception {
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_ADD, null);
    assertThat(shadowOf(textToSpeech).getQueueMode()).isEqualTo(TextToSpeech.QUEUE_ADD);
  }
}
