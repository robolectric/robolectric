package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.IRecognitionService;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import java.util.Queue;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Robolectric shadow for SpeechRecognizer. */
@Implements(SpeechRecognizer.class)
public class ShadowSpeechRecognizer {

  @RealObject SpeechRecognizer realSpeechRecognizer;
  private static SpeechRecognizer latestSpeechRecognizer;
  private RecognitionListener recognitionListener;

  /**
   * Returns the latest SpeechRecognizer. This method can only be called after {@link
   * SpeechRecognizer#createSpeechRecognizer()} is called.
   */
  public static SpeechRecognizer getLatestSpeechRecognizer() {
    return latestSpeechRecognizer;
  }

  @Resetter
  public static void reset() {
    latestSpeechRecognizer = null;
  }

  @Implementation
  protected static SpeechRecognizer createSpeechRecognizer(
      final Context context, final ComponentName serviceComponent) {
    SpeechRecognizer result =
        reflector(SpeechRecognizerReflector.class)
            .createSpeechRecognizer(context, serviceComponent);
    latestSpeechRecognizer = result;
    return result;
  }

  @Implementation
  protected void startListening(Intent recognizerIntent) {
    // the real implementation connects to a service
    // simulate the resulting behavior once the service is connected
    Handler mainHandler = new Handler(Looper.getMainLooper());
    // perform the onServiceConnected logic
    mainHandler.post(
        () -> {
          SpeechRecognizerReflector recognizerReflector =
              reflector(SpeechRecognizerReflector.class, realSpeechRecognizer);
          recognizerReflector.setService(
              ReflectionHelpers.createNullProxy(IRecognitionService.class));
          Queue<Message> pendingTasks = recognizerReflector.getPendingTasks();
          while (!pendingTasks.isEmpty()) {
            recognizerReflector.getHandler().sendMessage(pendingTasks.poll());
          }
        });
  }

  /**
   * Handles changing the listener and allows access to the internal listener to trigger events and
   * sets the latest SpeechRecognizer.
   */
  @Implementation
  protected void handleChangeListener(RecognitionListener listener) {
    recognitionListener = listener;
  }

  public void triggerOnEndOfSpeech() {
    recognitionListener.onEndOfSpeech();
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

  /** Reflector interface for {@link SpeechRecognizer}'s internals. */
  @ForType(SpeechRecognizer.class)
  interface SpeechRecognizerReflector {

    @Static
    @Direct
    SpeechRecognizer createSpeechRecognizer(Context context, ComponentName serviceComponent);

    @Accessor("mService")
    void setService(IRecognitionService service);

    @Accessor("mPendingTasks")
    Queue<Message> getPendingTasks();

    @Accessor("mHandler")
    Handler getHandler();
  }
}
