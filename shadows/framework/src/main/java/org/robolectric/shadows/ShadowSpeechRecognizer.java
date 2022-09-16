package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.IRecognitionService;
import android.speech.RecognitionListener;
import android.speech.RecognitionSupport;
import android.speech.RecognitionSupportCallback;
import android.speech.SpeechRecognizer;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import java.util.Queue;
import java.util.concurrent.Executor;
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
  protected static SpeechRecognizer latestSpeechRecognizer;
  private Intent recognizerIntent;
  private RecognitionListener recognitionListener;
  private static boolean isOnDeviceRecognitionAvailable = true;

  private RecognitionSupportCallback recognitionSupportCallback;
  private Executor recognitionSupportExecutor;
  @Nullable private Intent latestModelDownloadIntent;

  /**
   * Returns the latest SpeechRecognizer. This method can only be called after {@link
   * SpeechRecognizer#createSpeechRecognizer()} is called.
   */
  public static SpeechRecognizer getLatestSpeechRecognizer() {
    return latestSpeechRecognizer;
  }

  /** Returns the argument passed to the last call to {@link SpeechRecognizer#startListening}. */
  public Intent getLastRecognizerIntent() {
    return recognizerIntent;
  }

  @Resetter
  public static void reset() {
    latestSpeechRecognizer = null;
    isOnDeviceRecognitionAvailable = true;
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
    this.recognizerIntent = recognizerIntent;
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

  public void triggerOnReadyForSpeech(Bundle bundle) {
    recognitionListener.onReadyForSpeech(bundle);
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

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected static SpeechRecognizer createOnDeviceSpeechRecognizer(final Context context) {
    SpeechRecognizer result =
        reflector(SpeechRecognizerReflector.class).createOnDeviceSpeechRecognizer(context);
    latestSpeechRecognizer = result;
    return result;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected static boolean isOnDeviceRecognitionAvailable(final Context context) {
    return isOnDeviceRecognitionAvailable;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void checkRecognitionSupport(
      Intent recognizerIntent, Executor executor, RecognitionSupportCallback supportListener) {
    recognitionSupportExecutor = executor;
    recognitionSupportCallback = supportListener;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected void triggerModelDownload(Intent recognizerIntent) {
    latestModelDownloadIntent = recognizerIntent;
  }

  public static void setIsOnDeviceRecognitionAvailable(boolean available) {
    isOnDeviceRecognitionAvailable = available;
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  public void triggerSupportResult(RecognitionSupport recognitionSupport) {
    recognitionSupportExecutor.execute(
        () -> recognitionSupportCallback.onSupportResult(recognitionSupport));
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  public void triggerSupportError(int error) {
    recognitionSupportExecutor.execute(() -> recognitionSupportCallback.onError(error));
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  @Nullable
  public Intent getLatestModelDownloadIntent() {
    return latestModelDownloadIntent;
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

    @Static
    @Direct
    SpeechRecognizer createOnDeviceSpeechRecognizer(Context context);
  }
}
