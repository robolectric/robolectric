package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link ShadowSpeechRecognizer}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowSpeechRecognizerTest {
  private SpeechRecognizer speechRecognizer;
  private static Bundle bundleRecieved;
  private static float rmsdbRecieved;
  private static int errorRecieved;

  @Before
  public void setUp() {
    speechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(ApplicationProvider.getApplicationContext());
    shadowOf(speechRecognizer).handleChangeListener(new TestRecognitionListener());
  }

  @Test
  public void onErrorCalled() throws Exception {
    int expectedError = 1;

    shadowOf(speechRecognizer).triggerOnError(expectedError);
    shadowOf(getMainLooper()).idle();

    assertThat(errorRecieved).isEqualTo(expectedError);
  }

  @Test
  public void onPartialResultsCalled() throws Exception {
    Bundle expectedBundle = new Bundle();
    ArrayList<String> results = new ArrayList<>();
    String result = "onPartialResult";
    results.add(result);
    expectedBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, results);

    shadowOf(speechRecognizer).triggerOnPartialResults(expectedBundle);
    shadowOf(getMainLooper()).idle();

    assertThat(bundleRecieved).isEqualTo(expectedBundle);
  }

  @Test
  public void onResultCalled() throws Exception {
    Bundle expectedBundle = new Bundle();
    ArrayList<String> results = new ArrayList<>();
    String result = "onResult";
    results.add(result);
    expectedBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, results);

    shadowOf(speechRecognizer).triggerOnResults(expectedBundle);
    shadowOf(getMainLooper()).idle();

    assertThat(bundleRecieved).isEqualTo(expectedBundle);
  }

  @Test
  public void onRmsChangedCalled() throws Exception {
    float expectedRmsdB = 1.0f;

    shadowOf(speechRecognizer).triggerOnRmsChanged(expectedRmsdB);
    shadowOf(getMainLooper()).idle();

    assertThat(rmsdbRecieved).isEqualTo(expectedRmsdB);
  }

  @Test
  public void startAndStopListening() throws Exception {
    // Check that start and stop listening methods in SpeechRecognizer don't break.
    speechRecognizer.startListening(new Intent());
    shadowOf(speechRecognizer).triggerOnResults(new Bundle());
    shadowOf(getMainLooper()).idle();
    speechRecognizer.stopListening();
  }

  static final class TestRecognitionListener implements RecognitionListener {

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int error) {
      errorRecieved = error;
    }

    @Override
    public void onEvent(int eventType, Bundle params) {}

    @Override
    public void onPartialResults(Bundle bundle) {
      bundleRecieved = bundle;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {}

    @Override
    public void onResults(Bundle bundle) {
      bundleRecieved = bundle;
    }

    @Override
    public void onRmsChanged(float rmsdB) {
      rmsdbRecieved = rmsdB;
    }
  }
}
