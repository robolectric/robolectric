package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.service.voice.AlwaysOnHotwordDetector.EventPayload.DATA_FORMAT_TRIGGER_AUDIO;
import static android.service.voice.AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.soundtrigger.KeyphraseEnrollmentInfo;
import android.hardware.soundtrigger.KeyphraseMetadata;
import android.hardware.soundtrigger.SoundTrigger.KeyphraseRecognitionExtra;
import android.media.AudioFormat;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.os.SharedMemory;
import android.service.voice.AlwaysOnHotwordDetector;
import android.service.voice.AlwaysOnHotwordDetector.Callback;
import android.service.voice.AlwaysOnHotwordDetector.EventPayload;
import android.service.voice.HotwordDetectedResult;
import android.service.voice.IVoiceInteractionService;
import com.android.internal.app.IVoiceInteractionManagerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Shadow implementation of {@link android.service.voice.AlwaysOnHotwordDetector}. */
@Implements(value = AlwaysOnHotwordDetector.class, isInAndroidSdk = false)
public class ShadowAlwaysOnHotwordDetector {

  @RealObject private AlwaysOnHotwordDetector realObject;

  @Implementation(maxSdk = Q)
  protected void __constructor__(
      String text,
      Locale locale,
      AlwaysOnHotwordDetector.Callback callback,
      KeyphraseEnrollmentInfo keyphraseEnrollmentInfo,
      IVoiceInteractionService voiceInteractionService,
      IVoiceInteractionManagerService modelManagementService) {
    invokeConstructor(
        AlwaysOnHotwordDetector.class,
        realObject,
        from(String.class, text),
        from(Locale.class, locale),
        from(AlwaysOnHotwordDetector.Callback.class, callback),
        from(KeyphraseEnrollmentInfo.class, keyphraseEnrollmentInfo),
        from(IVoiceInteractionService.class, voiceInteractionService),
        from(IVoiceInteractionManagerService.class, modelManagementService));
    setEnrollmentFields(text, locale, keyphraseEnrollmentInfo);
  }

  @Implementation(minSdk = R, maxSdk = R)
  protected void __constructor__(
      String text,
      Locale locale,
      AlwaysOnHotwordDetector.Callback callback,
      KeyphraseEnrollmentInfo keyphraseEnrollmentInfo,
      IVoiceInteractionManagerService modelManagementService) {
    invokeConstructor(
        AlwaysOnHotwordDetector.class,
        realObject,
        from(String.class, text),
        from(Locale.class, locale),
        from(AlwaysOnHotwordDetector.Callback.class, callback),
        from(KeyphraseEnrollmentInfo.class, keyphraseEnrollmentInfo),
        from(IVoiceInteractionManagerService.class, modelManagementService));
    setEnrollmentFields(text, locale, keyphraseEnrollmentInfo);
  }

  @Implementation(minSdk = S, maxSdk = TIRAMISU)
  protected void __constructor__(
      String text,
      Locale locale,
      AlwaysOnHotwordDetector.Callback callback,
      KeyphraseEnrollmentInfo keyphraseEnrollmentInfo,
      IVoiceInteractionManagerService modelManagementService,
      int targetSdkVersion,
      boolean supportHotwordDetectionService,
      PersistableBundle options,
      SharedMemory sharedMemory) {
    invokeConstructor(
        AlwaysOnHotwordDetector.class,
        realObject,
        from(String.class, text),
        from(Locale.class, locale),
        from(AlwaysOnHotwordDetector.Callback.class, callback),
        from(KeyphraseEnrollmentInfo.class, keyphraseEnrollmentInfo),
        from(IVoiceInteractionManagerService.class, modelManagementService),
        from(int.class, targetSdkVersion),
        from(boolean.class, supportHotwordDetectionService),
        from(PersistableBundle.class, options),
        from(SharedMemory.class, sharedMemory));
    setEnrollmentFields(text, locale, keyphraseEnrollmentInfo);
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
  protected void __constructor__(
      String text,
      Locale locale,
      Executor executor,
      AlwaysOnHotwordDetector.Callback callback,
      KeyphraseEnrollmentInfo keyphraseEnrollmentInfo,
      IVoiceInteractionManagerService modelManagementService,
      int targetSdkVersion,
      boolean supportSandboxedDetectionService) {
    invokeConstructor(
        AlwaysOnHotwordDetector.class,
        realObject,
        from(String.class, text),
        from(Locale.class, locale),
        from(Executor.class, executor),
        from(AlwaysOnHotwordDetector.Callback.class, callback),
        from(KeyphraseEnrollmentInfo.class, keyphraseEnrollmentInfo),
        from(IVoiceInteractionManagerService.class, modelManagementService),
        from(int.class, targetSdkVersion),
        from(boolean.class, supportSandboxedDetectionService));
    setEnrollmentFields(text, locale, keyphraseEnrollmentInfo);
  }

  /** Invokes Callback#onError. */
  public void triggerOnErrorCallback() {
    reflector(AlwaysOnHotwordDetectorReflector.class, realObject).getCallback().onError();
  }

  /** Invokes Callback#onDetected. */
  public void triggerOnDetectedCallback(EventPayload eventPayload) {
    reflector(AlwaysOnHotwordDetectorReflector.class, realObject)
        .getCallback()
        .onDetected(eventPayload);
  }

  /** Invokes Callback#onAvailabilityChanged. */
  public void triggerOnAvailabilityChangedCallback(int status) {
    reflector(AlwaysOnHotwordDetectorReflector.class, realObject)
        .getCallback()
        .onAvailabilityChanged(status);
  }

  private void setEnrollmentFields(
      String text, Locale locale, KeyphraseEnrollmentInfo keyphraseEnrollmentInfo) {
    reflector(AlwaysOnHotwordDetectorReflector.class, realObject)
        .setAvailability(STATE_KEYPHRASE_ENROLLED);
    if (RuntimeEnvironment.getApiLevel() > Q && keyphraseEnrollmentInfo != null) {
      reflector(AlwaysOnHotwordDetectorReflector.class, realObject)
          .setKeyphraseMetadata(keyphraseEnrollmentInfo.getKeyphraseMetadata(text, locale));
    }
  }

  /** Shadow for AsyncTask kicked off in the constructor of AlwaysOnHotwordDetector. */
  @Implements(
      className = "android.service.voice.AlwaysOnHotwordDetector$RefreshAvailabiltyTask",
      maxSdk = TIRAMISU,
      isInAndroidSdk = false)
  @SuppressWarnings("robolectric.mismatchedTypes")
  public static class ShadowRefreshAvailabilityTask<Params, Progress, Result>
      extends ShadowPausedAsyncTask<Params, Progress, Result> {

    @Implementation
    protected int internalGetInitialAvailability() {
      return STATE_KEYPHRASE_ENROLLED;
    }

    @Implementation(maxSdk = Q)
    protected boolean internalGetIsEnrolled(int keyphraseId, Locale locale) {
      return true;
    }

    @Implementation(minSdk = R)
    protected void internalUpdateEnrolledKeyphraseMetadata() {
      // No-op, we already set this field in #setEnrollmentFields()
    }
  }

  /** Invokes the normally hidden EventPayload constructor for passing to Callback#onDetected(). */
  public static EventPayload createEventPayload(
      boolean triggerAvailable,
      boolean captureAvailable,
      AudioFormat audioFormat,
      int captureSession,
      byte[] data) {
    if (RuntimeEnvironment.getApiLevel() <= Q) {
      return reflector(EventPayloadReflector.class)
          .newEventPayload(triggerAvailable, captureAvailable, audioFormat, captureSession, data);
    } else if (RuntimeEnvironment.getApiLevel() == TIRAMISU) {
      return reflector(EventPayloadReflector.class)
          .newEventPayload(
              captureAvailable,
              audioFormat,
              captureSession,
              DATA_FORMAT_TRIGGER_AUDIO,
              data,
              null,
              null,
              new ArrayList<>());
    } else {
      return reflector(EventPayloadReflector.class)
          .newEventPayload(
              captureAvailable,
              audioFormat,
              captureSession,
              DATA_FORMAT_TRIGGER_AUDIO,
              data,
              null,
              null,
              new ArrayList<>(),
              0);
    }
  }

  /** Accessor interface for AlwaysOnHotwordDetector's internals. */
  @ForType(AlwaysOnHotwordDetector.class)
  interface AlwaysOnHotwordDetectorReflector {

    // new constructor after U
    @Constructor
    AlwaysOnHotwordDetector newInstance(
        String text,
        Locale locale,
        Executor executor,
        AlwaysOnHotwordDetector.Callback callback,
        KeyphraseEnrollmentInfo keyphraseEnrollmentInfo,
        IVoiceInteractionManagerService modelManagementService,
        int targetSdkVersion,
        boolean supportSandboxedDetectionService,
        String attributionTag);

    @Constructor
    AlwaysOnHotwordDetector newInstance(
        String text,
        Locale locale,
        Executor executor,
        AlwaysOnHotwordDetector.Callback callback,
        KeyphraseEnrollmentInfo keyphraseEnrollmentInfo,
        IVoiceInteractionManagerService modelManagementService,
        int targetSdkVersion,
        boolean supportSandboxedDetectionService);

    @Accessor("mAvailability")
    void setAvailability(int availability);

    @Accessor("mKeyphraseMetadata")
    void setKeyphraseMetadata(KeyphraseMetadata keyphraseMetadata);

    @Accessor("mExternalCallback")
    Callback getCallback();
  }

  /** Accessor interface for inner class EventPayload which has a private constructor. */
  @ForType(AlwaysOnHotwordDetector.EventPayload.class)
  interface EventPayloadReflector {

    @Constructor
    EventPayload newEventPayload(
        boolean triggerAvailable,
        boolean captureAvailable,
        AudioFormat audioFormat,
        int captureSession,
        byte[] data);

    @Constructor
    EventPayload newEventPayload(
        boolean captureAvailable,
        AudioFormat audioFormat,
        int captureSession,
        int dataFormat,
        byte[] data,
        @Nullable HotwordDetectedResult hotwordDetectedResult,
        @Nullable ParcelFileDescriptor audioStream,
        @Nonnull List<KeyphraseRecognitionExtra> keyphraseExtras);

    @Constructor
    EventPayload newEventPayload(
        boolean captureAvailable,
        AudioFormat audioFormat,
        int captureSession,
        int dataFormat,
        byte[] data,
        @Nullable HotwordDetectedResult hotwordDetectedResult,
        @Nullable ParcelFileDescriptor audioStream,
        @Nonnull List<KeyphraseRecognitionExtra> keyphraseExtras,
        long halEventReceivedMillis);
  }
}
