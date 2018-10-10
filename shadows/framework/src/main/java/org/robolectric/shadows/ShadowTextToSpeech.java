package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.UtteranceProgressListener;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TextToSpeech.class)
public class ShadowTextToSpeech {
  private Context context;
  private TextToSpeech.OnInitListener listener;
  private String lastSpokenText;
  private boolean shutdown = false;
  private boolean stopped = true;
  private int queueMode = -1;
  private UtteranceProgressListener utteranceProgressListener;

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
  protected int speak(final String text, final int queueMode, final HashMap<String, String> params) {
    return speak(
        text,
        queueMode,
        null,
        params == null ? null : params.get(Engine.KEY_PARAM_UTTERANCE_ID));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int speak(
      final CharSequence text, final int queueMode, final Bundle params, final String utteranceId) {
    stopped = false;
    lastSpokenText = text.toString();
    this.queueMode = queueMode;

    if (VERSION.SDK_INT >= ICE_CREAM_SANDWICH_MR1) {
      if (utteranceId != null && utteranceProgressListener != null) {
        utteranceProgressListener.onStart(utteranceId);
        utteranceProgressListener.onDone(utteranceId);
      }
    }
    return TextToSpeech.SUCCESS;
  }

  @Implementation(minSdk = ICE_CREAM_SANDWICH_MR1)
  protected int setOnUtteranceProgressListener(UtteranceProgressListener listener) {
    utteranceProgressListener = listener;
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