package org.robolectric.shadows;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

@Implements(TextToSpeech.class)
public class ShadowTextToSpeech {
  private Context context;
  private TextToSpeech.OnInitListener listener;
  private String lastSpokenText;
  private boolean shutdown = false;
  private boolean stopped = true;
  private int queueMode = -1;

  @Implementation
  public void __constructor__(Context context, TextToSpeech.OnInitListener listener) {
    this.context = context;
    this.listener = listener;
  }

  @Implementation
  public int speak(final String text, final int queueMode, final HashMap<String, String> params) {
    stopped = false;
    lastSpokenText = text;
    this.queueMode = queueMode;
    return TextToSpeech.SUCCESS;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int speak(
      final CharSequence text, final int queueMode, final Bundle params, final String utteranceId) {
    return speak(text.toString(), queueMode, new HashMap<>());
  }

  @Implementation
  public void shutdown() {
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
