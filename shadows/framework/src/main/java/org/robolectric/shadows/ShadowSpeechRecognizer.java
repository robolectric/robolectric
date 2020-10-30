package org.robolectric.shadows;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Robolectric shadow for SpeechRecognizer. */
@Implements(SpeechRecognizer.class)
public class ShadowSpeechRecognizer {

  private RecognitionListener recognitionListener;

  /** Handles setting the listener directly. */
  @Implementation
  protected void handleChangeListener(RecognitionListener listener) {
    recognitionListener = listener;
  }

  public void triggerOnError(int error) {
    recognitionListener.onError(error);
  }

  public void triggerOnPartialResults(Bundle bundle) {
    recognitionListener.onPartialResults(bundle);
  }

  public void triggerOnResults(Bundle bundle) {
    recognitionListener.onResults(bundle);
  }

  public void triggerOnRmsChanged(float rmsdB) {
    recognitionListener.onRmsChanged(rmsdB);
  }
}
