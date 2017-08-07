package org.robolectric.shadows;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.HashMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TextToSpeech.class)
public class ShadowTextToSpeech {
  private Context context;
  private TextToSpeech.OnInitListener listener;
  private String lastSpokenText;
  private boolean shutdown = false;
  private int queueMode = -1;

  @Implementation
  public void __constructor__(Context context, TextToSpeech.OnInitListener listener) {
    this.context = context;
    this.listener = listener;
  }

  @Implementation
  public int speak(final String text, final int queueMode, final HashMap<String, String> params) {
    lastSpokenText = text;
    this.queueMode = queueMode;
    return TextToSpeech.SUCCESS;
  }

  @Implementation
  public void shutdown() {
    shutdown = true;
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

  public int getQueueMode() {
    return queueMode;
  }
}
