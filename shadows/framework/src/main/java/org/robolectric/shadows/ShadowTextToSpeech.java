package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.UtteranceProgressListener;
import java.util.HashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(TextToSpeech.class)
public class ShadowTextToSpeech {

  @RealObject private TextToSpeech tts;

  private Context context;
  private TextToSpeech.OnInitListener listener;
  private String lastSpokenText;
  private boolean shutdown = false;
  private boolean stopped = true;
  private int queueMode = -1;

  @Implementation
  protected void __constructor__(Context context, TextToSpeech.OnInitListener listener) {
    this.context = context;
    this.listener = listener;
  }

  /**
   * Speaks the string using the specified queuing strategy and speech parameters.
   *
   * @param params The real implementation converts the hashmap into a bundle, but the bundle
   *     argument is not used in the shadow implementation.
   */
  @Implementation
  protected int speak(
      final String text, final int queueMode, final HashMap<String, String> params) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      return Shadow.directlyOn(tts, TextToSpeech.class).speak(text, queueMode, params);
    }
    return speak(
        text, queueMode, null, params == null ? null : params.get(Engine.KEY_PARAM_UTTERANCE_ID));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int speak(
      final CharSequence text, final int queueMode, final Bundle params, final String utteranceId) {
    stopped = false;
    lastSpokenText = text.toString();
    this.queueMode = queueMode;

    if (RuntimeEnvironment.getApiLevel() >= ICE_CREAM_SANDWICH_MR1) {
      if (utteranceId != null) {
        // The onStart and onDone callbacks are normally delivered asynchronously. Since in
        // Robolectric we don't need the wait for TTS package, the asynchronous callbacks are
        // simulated by posting it on a handler. The behavior of the callback can be changed for
        // each individual test by changing the idling mode of the foreground scheduler.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(
            () -> {
              UtteranceProgressListener utteranceProgressListener = getUtteranceProgressListener();
              if (utteranceProgressListener != null) {
                utteranceProgressListener.onStart(utteranceId);
              }
              // The onDone callback is posted in a separate run-loop from onStart, so that tests
              // can pause the scheduler and test the behavior between these two callbacks.
              handler.post(
                  () -> {
                    UtteranceProgressListener utteranceProgressListener2 =
                        getUtteranceProgressListener();
                    if (utteranceProgressListener2 != null) {
                      utteranceProgressListener2.onDone(utteranceId);
                    }
                  });
            });
      }
    }
    return TextToSpeech.SUCCESS;
  }

  @Implementation
  protected void shutdown() {
    shutdown = true;
  }

  @Implementation
  protected int stop() {
    stopped = true;
    return TextToSpeech.SUCCESS;
  }

  private UtteranceProgressListener getUtteranceProgressListener() {
    return ReflectionHelpers.getField(tts, "mUtteranceProgressListener");
  }

  public Context getContext() {
    return context;
  }

  public TextToSpeech.OnInitListener getOnInitListener() {
    return listener;
  }

  public String getLastSpokenText() {
    return lastSpokenText;
  }

  public void clearLastSpokenText() {
    lastSpokenText = null;
  }

  public boolean isShutdown() {
    return shutdown;
  }

  /** @return {@code true} if the TTS is stopped. */
  public boolean isStopped() {
    return stopped;
  }

  public int getQueueMode() {
    return queueMode;
  }
}
