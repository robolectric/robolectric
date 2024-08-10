package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.speech.IRecognitionService;
import android.speech.RecognitionListener;
import android.speech.RecognitionSupport;
import android.speech.RecognitionSupportCallback;
import android.speech.SpeechRecognizer;
import com.google.common.base.Preconditions;
import java.util.Queue;
import java.util.concurrent.Executor;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.InDevelopment;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.U;

/** Robolectric shadow for SpeechRecognizer. */
@Implements(value = SpeechRecognizer.class)
public class ShadowSpeechRecognizer {

  @SuppressWarnings("NonFinalStaticField")
  private static SpeechRecognizer latestSpeechRecognizer;

  @SuppressWarnings("NonFinalStaticField")
  private static boolean isOnDeviceRecognitionAvailable = true;

  @RealObject SpeechRecognizer realSpeechRecognizer;

  // NOTE: Do not manipulate state directly in this class. Call {@link #getState()} instead.
  private final ShadowSpeechRecognizerState state = new ShadowSpeechRecognizerState();

  @Resetter
  public static void reset() {
    latestSpeechRecognizer = null;
    isOnDeviceRecognitionAvailable = true;
  }

  /**
   * Returns the latest SpeechRecognizer. This method can only be called after {@link
   * SpeechRecognizer#createSpeechRecognizer(Context)} is called.
   */
  public static SpeechRecognizer getLatestSpeechRecognizer() {
    return latestSpeechRecognizer;
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

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected static SpeechRecognizer createOnDeviceSpeechRecognizer(final Context context) {
    SpeechRecognizer result =
        reflector(SpeechRecognizerReflector.class).createOnDeviceSpeechRecognizer(context);
    latestSpeechRecognizer = result;
    return result;
  }

  public static void setIsOnDeviceRecognitionAvailable(boolean available) {
    isOnDeviceRecognitionAvailable = available;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected static boolean isOnDeviceRecognitionAvailable(final Context context) {
    return isOnDeviceRecognitionAvailable;
  }

  /**
   * Returns the state of this shadow instance.
   *
   * <p>Subclasses may override this function to customize which state is returned.
   */
  protected ShadowSpeechRecognizerState getState() {
    return state;
  }

  /**
   * Returns the {@link ShadowSpeechRecognizerDirectAccessors} implementation that can handle direct
   * access to functions/variables of a real {@link SpeechRecognizer}.
   *
   * <p>Subclasses may override this function to customize access in case they are shadowing a
   * subclass of {@link SpeechRecognizer} that functions differently than the parent class.
   */
  protected ShadowSpeechRecognizerDirectAccessors getDirectAccessors() {
    return reflector(SpeechRecognizerReflector.class, realSpeechRecognizer);
  }

  /** Returns true iff the destroy method of was invoked for the recognizer. */
  public boolean isDestroyed() {
    return getState().isRecognizerDestroyed;
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected void destroy() {
    getState().isRecognizerDestroyed = true;
    getDirectAccessors().destroy();
  }

  /** Returns the argument passed to the last call to {@link SpeechRecognizer#startListening}. */
  public Intent getLastRecognizerIntent() {
    return getState().recognizerIntent;
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected void startListening(Intent recognizerIntent) {
    // Record the most recent requested intent.
    ShadowSpeechRecognizerState shadowState = getState();
    shadowState.recognizerIntent = recognizerIntent;

    // From the implementation of {@link SpeechRecognizer#startListening} it seems that it allows
    // running the method on an already destroyed object, so we replicate the same by resetting
    // isRecognizerDestroyed.
    shadowState.isRecognizerDestroyed = false;

    // The real implementation connects to a service simulate the resulting behavior once
    // the service is connected.
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              ShadowSpeechRecognizerDirectAccessors directAccessors = getDirectAccessors();
              directAccessors.setService(createFakeSpeechRecognitionService());

              Handler taskHandler = directAccessors.getHandler();
              Queue<Message> pendingTasks = directAccessors.getPendingTasks();
              while (!pendingTasks.isEmpty()) {
                taskHandler.sendMessage(pendingTasks.poll());
              }
            });
  }

  /** Handles changing the listener and allows access to the internal listener to trigger events. */
  @Implementation(maxSdk = U.SDK_INT) // TODO(hoisie): Update this to support Android V
  @InDevelopment
  protected void handleChangeListener(RecognitionListener listener) {
    getState().recognitionListener = listener;
  }

  public void triggerOnEndOfSpeech() {
    getState().recognitionListener.onEndOfSpeech();
  }

  public void triggerOnError(int error) {
    getState().recognitionListener.onError(error);
  }

  public void triggerOnReadyForSpeech(Bundle bundle) {
    getState().recognitionListener.onReadyForSpeech(bundle);
  }

  public void triggerOnPartialResults(Bundle bundle) {
    getState().recognitionListener.onPartialResults(bundle);
  }

  public void triggerOnResults(Bundle bundle) {
    getState().recognitionListener.onResults(bundle);
  }

  public void triggerOnRmsChanged(float rmsdB) {
    getState().recognitionListener.onRmsChanged(rmsdB);
  }

  @RequiresApi(api = VERSION_CODES.TIRAMISU)
  @Implementation(minSdk = VERSION_CODES.TIRAMISU, maxSdk = U.SDK_INT)
  protected void checkRecognitionSupport(
      @NonNull Intent recognizerIntent,
      @NonNull Executor executor,
      @NonNull @ClassName("android.speech.RecognitionSupportCallback") Object supportListener) {
    Preconditions.checkArgument(supportListener instanceof RecognitionSupportCallback);

    ShadowSpeechRecognizerState shadowState = getState();
    shadowState.recognitionSupportExecutor = (Executor) executor;
    shadowState.recognitionSupportCallback = supportListener;
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  @Nullable
  public Intent getLatestModelDownloadIntent() {
    return getState().latestModelDownloadIntent;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU, maxSdk = U.SDK_INT)
  protected void triggerModelDownload(Intent recognizerIntent) {
    getState().latestModelDownloadIntent = recognizerIntent;
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  public void triggerSupportResult(/*RecognitionSupport*/ Object recognitionSupport) {
    Preconditions.checkArgument(recognitionSupport instanceof RecognitionSupport);

    ShadowSpeechRecognizerState shadowState = getState();
    shadowState.recognitionSupportExecutor.execute(
        () ->
            ((RecognitionSupportCallback) shadowState.recognitionSupportCallback)
                .onSupportResult((RecognitionSupport) recognitionSupport));
  }

  @RequiresApi(VERSION_CODES.TIRAMISU)
  public void triggerSupportError(int error) {
    ShadowSpeechRecognizerState shadowState = getState();
    shadowState.recognitionSupportExecutor.execute(
        () -> ((RecognitionSupportCallback) shadowState.recognitionSupportCallback).onError(error));
  }

  /**
   * {@link SpeechRecognizer} implementation now checks if the service's binder is alive whenever
   * {@link SpeechRecognizer#checkOpenConnection} is called. This means that we need to return a
   * deeper proxy that returns a delegating proxy that always reports the binder as alive.
   */
  private static IRecognitionService createFakeSpeechRecognitionService() {
    return ReflectionHelpers.createDelegatingProxy(
        IRecognitionService.class, new AlwaysAliveSpeechRecognitionServiceDelegate());
  }

  /**
   * A proxy delegate for {@link IRecognitionService} that always returns a delegating proxy that
   * returns an {@link AlwaysAliveBinderDelegate} when {@link IRecognitionService#asBinder()} is
   * called.
   *
   * @see #createFakeSpeechRecognitionService() for more details
   */
  private static class AlwaysAliveSpeechRecognitionServiceDelegate {
    public IBinder asBinder() {
      return ReflectionHelpers.createDelegatingProxy(
          IBinder.class, new AlwaysAliveBinderDelegate());
    }
  }

  /**
   * A proxy delegate for {@link IBinder} that always returns when {@link IBinder#isBinderAlive()}
   * is called.
   *
   * @see #createFakeSpeechRecognitionService() for more details
   */
  private static class AlwaysAliveBinderDelegate {
    public boolean isBinderAlive() {
      return true;
    }
  }

  /**
   * The state of a specific instance of {@link ShadowSpeechRecognizer}.
   *
   * <p>NOTE: Not stored as variables in the parent class itself since subclasses may need to return
   * a different instance of this class to operate on.
   *
   * <p>NOTE: This class is public since custom shadows may reside in a different package.
   */
  public static class ShadowSpeechRecognizerState {
    private boolean isRecognizerDestroyed = false;
    private Intent recognizerIntent;
    private RecognitionListener recognitionListener;
    private Executor recognitionSupportExecutor;
    private /*RecognitionSupportCallback*/ Object recognitionSupportCallback;
    @Nullable private Intent latestModelDownloadIntent;
  }

  /**
   * An interface to access direct functions/variables of an instance of {@link SpeechRecognizer}.
   *
   * <p>Abstracted to allow subclasses to return customized accessors.
   */
  protected interface ShadowSpeechRecognizerDirectAccessors {
    /**
     * Invokes {@link SpeechRecognizer#destroy()} on a real instance of {@link SpeechRecognizer}.
     */
    void destroy();

    /** Sets the {@link IRecognitionService} used by a real {@link SpeechRecognizer}. */
    void setService(IRecognitionService service);

    /** Returns a {@link Queue} of pending async tasks of a real {@link SpeechRecognizer}. */
    Queue<Message> getPendingTasks();

    /**
     * Returns the {@link Handler} of a real {@link SpeechRecognizer} that it uses to process any
     * pending async tasks returned by {@link #getPendingTasks()}.
     */
    Handler getHandler();
  }

  /** Reflector interface for {@link SpeechRecognizer}'s internals. */
  @ForType(SpeechRecognizer.class)
  interface SpeechRecognizerReflector extends ShadowSpeechRecognizerDirectAccessors {

    @Static
    @Direct
    SpeechRecognizer createSpeechRecognizer(Context context, ComponentName serviceComponent);

    @Static
    @Direct
    SpeechRecognizer createOnDeviceSpeechRecognizer(Context context);

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
