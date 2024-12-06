package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.annotation.RequiresApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Message;
import android.speech.IRecognitionService;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import java.util.Queue;
import java.util.concurrent.Executor;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.V;

/**
 * Robolectric shadow for SpeechRecognizerImpl.
 *
 * <p>Prior to Android V, SpeechRecognizer contained all functionality within one class. The {@link
 * ShadowSpeechRecognizer} shadow would work correctly to shadow both static and instance functions
 * of the class. With Android V, the instance of {@link SpeechRecognizer} returned from {@link
 * SpeechRecognizer#createSpeechRecognizer(Context)} is an instance of {@link
 * android.speech.SpeechRecognizerProxy} which delegates all calls to {@link
 * android.speech.SpeechRecognizerImpl}. We need this shadow in order to intercept function calls
 * correctly and to ensure that the functionality of {@link ShadowSpeechRecognizer} still works in
 * tests prior to Android V and on Android V+.
 *
 * <p>Customizations for this implementation:
 *
 * <ul>
 *   <li>Intercept of all needed functions in SpeechRecognizerImpl
 *   <li>Direct access to functions/variables in SpeechRecognizerImpl via {@link
 *       #getDirectAccessors()}
 *   <li>Parent shadow class' state is used for state (no custom state)
 * </ul>
 */
@Implements(
    className = ShadowSpeechRecognizerImpl.CLASS_NAME,
    isInAndroidSdk = false,
    minSdk = V.SDK_INT)
public class ShadowSpeechRecognizerImpl extends ShadowSpeechRecognizer {
  protected static final String CLASS_NAME = "android.speech.SpeechRecognizerImpl";

  @RealObject SpeechRecognizer realSpeechRecognizer;

  @Override
  protected ShadowSpeechRecognizerDirectAccessors getDirectAccessors() {
    return reflector(SpeechRecognizerImplReflector.class, realSpeechRecognizer);
  }

  /** Provides access to the shadow's state for other shadows in the same package. */
  ShadowSpeechRecognizerState internalGetState() {
    return super.getState();
  }

  @Implementation
  @Override
  protected void destroy() {
    super.destroy();
  }

  @Implementation
  @Override
  protected void startListening(Intent recognizerIntent) {
    super.startListening(recognizerIntent);
  }

  @Implementation
  @Override
  protected void handleChangeListener(RecognitionListener listener) {
    super.handleChangeListener(listener);
  }

  @RequiresApi(api = VERSION_CODES.TIRAMISU)
  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  @Override
  protected void checkRecognitionSupport(
      @NonNull Intent recognizerIntent,
      @NonNull Executor executor,
      @NonNull @ClassName("android.speech.RecognitionSupportCallback") Object supportListener) {
    super.checkRecognitionSupport(recognizerIntent, executor, supportListener);
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  @Override
  protected void triggerModelDownload(Intent recognizerIntent) {
    super.triggerModelDownload(recognizerIntent);
  }

  /** Reflector interface for {@link android.speech.SpeechRecognizerImpl}'s internals. */
  @ForType(className = CLASS_NAME)
  interface SpeechRecognizerImplReflector extends ShadowSpeechRecognizerDirectAccessors {
    @Direct
    @Override
    void destroy();

    @Accessor("mService")
    @Override
    void setService(IRecognitionService service);

    @Accessor("mPendingTasks")
    @Override
    Queue<Message> getPendingTasks();

    @Accessor("mHandler")
    @Override
    Handler getHandler();
  }
}
