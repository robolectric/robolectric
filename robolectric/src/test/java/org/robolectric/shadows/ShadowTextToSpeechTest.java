package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.UtteranceProgressListener;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowTextToSpeechTest {
  private TextToSpeech textToSpeech;
  private Activity activity;
  private TextToSpeech.OnInitListener listener;
  private UtteranceProgressListener mockListener;

  @Before
  public void setUp() throws Exception {
    activity = Robolectric.buildActivity(Activity.class).create().get();
    listener = new TextToSpeech.OnInitListener() {
      @Override public void onInit(int i) {
      }
    };

    mockListener = mock(UtteranceProgressListener.class);
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
  public void isStopped_shouldReturnTrueBeforeSpeak() throws Exception {
    assertThat(shadowOf(textToSpeech).isStopped()).isTrue();
  }

  @Test
  public void isStopped_shouldReturnTrueAfterStop() throws Exception {
    textToSpeech.stop();
    assertThat(shadowOf(textToSpeech).isStopped()).isTrue();
  }

  @Test
  public void isStopped_shouldReturnFalseAfterSpeak() throws Exception {
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    assertThat(shadowOf(textToSpeech).isStopped()).isFalse();
  }

  @Test
  public void getQueueMode_shouldReturnMostRecentQueueMode() throws Exception {
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_ADD, null);
    assertThat(shadowOf(textToSpeech).getQueueMode()).isEqualTo(TextToSpeech.QUEUE_ADD);
  }

  @Test
  public void threeArgumentSpeak_withUtteranceId_shouldGetCallbackUtteranceId() throws Exception {
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    HashMap<String, String> paramsMap = new HashMap<>();
    paramsMap.put(Engine.KEY_PARAM_UTTERANCE_ID, "ThreeArgument");
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, paramsMap);

    Robolectric.flushForegroundThreadScheduler();

    verify(mockListener).onStart("ThreeArgument");
    verify(mockListener).onDone("ThreeArgument");
  }

  @Test
  public void threeArgumentSpeak_withoutUtteranceId_shouldDoesNotGetCallback() throws Exception {
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);

    Robolectric.flushForegroundThreadScheduler();

    verify(mockListener, never()).onStart(null);
    verify(mockListener, never()).onDone(null);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void speak_withUtteranceId_shouldReturnSpokenText() throws Exception {
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null, "TTSEnable");
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isEqualTo("Hello");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void onUtteranceProgressListener_shouldGetCallbackUtteranceId() throws Exception {
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null, "TTSEnable");

    Robolectric.flushForegroundThreadScheduler();

    verify(mockListener).onStart("TTSEnable");
    verify(mockListener).onDone("TTSEnable");
  }
}
