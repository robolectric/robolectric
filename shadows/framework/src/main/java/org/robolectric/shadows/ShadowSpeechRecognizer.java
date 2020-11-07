package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.IRecognitionService;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Robolectric shadow for SpeechRecognizer. */
@Implements(SpeechRecognizer.class)
public class ShadowSpeechRecognizer {

  @RealObject SpeechRecognizer realObject;
  private RecognitionListener recognitionListener;

  @Implementation
  protected void startListening(Intent recognizerIntent) {
    // simulate the response to the real startListening's bindService call
    SpeechRecognizerReflector reflector = reflector(SpeechRecognizerReflector.class, realObject);
    Binder recognitionServiceBinder = new Binder();
    recognitionServiceBinder.attachInterface(
        ReflectionHelpers.createNullProxy(IRecognitionService.class),
        IRecognitionService.class.getName());

    Intent serviceIntent = new Intent(RecognitionService.SERVICE_INTERFACE);
    ComponentName componentName = reflector.getServiceComponent();
    if (componentName == null) {
      componentName = new ComponentName("org.robolectric", "FakeSpeechRecognizerService");
      ShadowSettings.ShadowSecure.putString(
          reflector.getContext().getContentResolver(),
          Settings.Secure.VOICE_RECOGNITION_SERVICE,
          componentName.flattenToString());
    }
    serviceIntent.setComponent(componentName);

    ShadowContextWrapper.getShadowInstrumentation()
        .setComponentNameAndServiceForBindServiceForIntent(
            serviceIntent, reflector.getServiceComponent(), recognitionServiceBinder);
    directlyOn(realObject, SpeechRecognizer.class).startListening(recognizerIntent);
  }

  /** Handles changing the listener and allows access to the internal listener to trigger events. */
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

  /** Accessor interface for {@link SpeechRecognizer}'s internals. */
  @ForType(SpeechRecognizer.class)
  interface SpeechRecognizerReflector {
    @Accessor("mConnection")
    Object getConnection();

    @Accessor("mServiceComponent")
    ComponentName getServiceComponent();

    @Accessor("mContext")
    Context getContext();
  }
}
