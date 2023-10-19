package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.soundtrigger.KeyphraseEnrollmentInfo;
import android.hardware.soundtrigger.KeyphraseMetadata;
import android.media.AudioFormat;
import android.service.voice.AlwaysOnHotwordDetector;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.app.IVoiceInteractionManagerService;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Test for ShadowAlwaysOnHotwordDetector. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = UPSIDE_DOWN_CAKE)
public class ShadowAlwaysOnHotwordDetectorTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  private Object mockCallback;
  @Captor private ArgumentCaptor<AlwaysOnHotwordDetector.EventPayload> payloadCaptor;

  @Before
  public void setUp() {
    mockCallback = mock(AlwaysOnHotwordDetector.Callback.class);
  }

  @Test
  public void testGetSupportedRecognitionModes() {
    AlwaysOnHotwordDetector detector = (AlwaysOnHotwordDetector) createDetector();
    assertThat(detector.getSupportedRecognitionModes())
        .isEqualTo(AlwaysOnHotwordDetector.RECOGNITION_MODE_VOICE_TRIGGER);
  }

  @Test
  public void testCallback_onError() {
    AlwaysOnHotwordDetector detector = (AlwaysOnHotwordDetector) createDetector();
    ShadowAlwaysOnHotwordDetector shadowDetector = Shadow.extract(detector);

    shadowDetector.triggerOnErrorCallback();
    verify((AlwaysOnHotwordDetector.Callback) mockCallback).onError();
  }

  @Test
  public void testCallback_onDetected() {
    AlwaysOnHotwordDetector detector = (AlwaysOnHotwordDetector) createDetector();
    ShadowAlwaysOnHotwordDetector shadowDetector = Shadow.extract(detector);

    AudioFormat audioFormat = new AudioFormat.Builder().setSampleRate(8000).build();
    byte[] data = new byte[0];
    int captureSession = 99;
    shadowDetector.triggerOnDetectedCallback(
        ShadowAlwaysOnHotwordDetector.createEventPayload(
            true, true, audioFormat, captureSession, data));
    verify((AlwaysOnHotwordDetector.Callback) mockCallback).onDetected(payloadCaptor.capture());

    assertThat(payloadCaptor.getValue().getCaptureAudioFormat()).isEqualTo(audioFormat);
    assertThat(payloadCaptor.getValue().getCaptureSession()).isEqualTo(captureSession);
    assertThat(payloadCaptor.getValue().getData()).isEqualTo(data);
  }

  @SuppressWarnings("ReturnValueIgnored")
  private Object createDetector() {
    KeyphraseMetadata keyphraseMetadata =
        new KeyphraseMetadata(
            1,
            "keyphrase",
            new HashSet<>(),
            AlwaysOnHotwordDetector.RECOGNITION_MODE_VOICE_TRIGGER);
    KeyphraseEnrollmentInfo keyphraseEnrollmentInfo = mock(KeyphraseEnrollmentInfo.class);
    when(keyphraseEnrollmentInfo.getKeyphraseMetadata(any(), any())).thenReturn(keyphraseMetadata);
    ShadowAlwaysOnHotwordDetector.AlwaysOnHotwordDetectorReflector accessor =
        reflector(ShadowAlwaysOnHotwordDetector.AlwaysOnHotwordDetectorReflector.class);
    try {
      // check if AlwaysOnHotwordDetector has the U and before constructor
      AlwaysOnHotwordDetector.class.getDeclaredConstructor(
          String.class,
          Locale.class,
          Executor.class,
          AlwaysOnHotwordDetector.Callback.class,
          KeyphraseEnrollmentInfo.class,
          IVoiceInteractionManagerService.class,
          int.class,
          boolean.class);
      return accessor.newInstance(
          "keyphrase",
          Locale.US,
          Executors.newSingleThreadExecutor(),
          (AlwaysOnHotwordDetector.Callback) mockCallback,
          keyphraseEnrollmentInfo,
          null,
          0,
          false);
    } catch (NoSuchMethodException e) {
      // Use the new constructor when the old constructor does not exist
      return accessor.newInstance(
          "keyphrase",
          Locale.US,
          Executors.newSingleThreadExecutor(),
          (AlwaysOnHotwordDetector.Callback) mockCallback,
          keyphraseEnrollmentInfo,
          null,
          0,
          false,
          "");
    }
  }
}
