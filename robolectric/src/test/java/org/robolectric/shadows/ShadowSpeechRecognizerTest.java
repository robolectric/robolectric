package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognitionSupport;
import android.speech.RecognitionSupportCallback;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog.LogItem;

/** Unit tests for {@link ShadowSpeechRecognizer}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowSpeechRecognizerTest {
  private SpeechRecognizer speechRecognizer;
  private TestRecognitionListener listener;
  private Context applicationContext;
  private TestRecognitionSupportCallback supportCallback;

  @Before
  public void setUp() {
    applicationContext = ApplicationProvider.getApplicationContext();
    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext);
    listener = new TestRecognitionListener();
    supportCallback = new TestRecognitionSupportCallback();
  }

  @Test
  public void onErrorCalled() {
    startListening();

    shadowOf(speechRecognizer).triggerOnError(-1);

    assertThat(listener.errorReceived).isEqualTo(-1);
  }

  @Test
  public void onReadyForSpeechCalled() {
    startListening();
    Bundle expectedBundle = new Bundle();
    ArrayList<String> results = new ArrayList<>();
    String result = "onReadyForSpeech";
    results.add(result);
    expectedBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, results);

    shadowOf(speechRecognizer).triggerOnReadyForSpeech(expectedBundle);

    assertThat(listener.bundleReceived).isEqualTo(expectedBundle);
  }

  @Test
  public void onPartialResultsCalled() {
    startListening();
    Bundle expectedBundle = new Bundle();
    ArrayList<String> results = new ArrayList<>();
    String result = "onPartialResult";
    results.add(result);
    expectedBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, results);

    shadowOf(speechRecognizer).triggerOnPartialResults(expectedBundle);

    assertThat(listener.bundleReceived).isEqualTo(expectedBundle);
  }

  @Test
  public void onEndOfSpeechCalled() {
    startListening();

    shadowOf(speechRecognizer).triggerOnEndOfSpeech();

    assertThat(listener.endofSpeechCalled).isTrue();
  }

  @Test
  public void onResultCalled() {
    startListening();
    Bundle expectedBundle = new Bundle();
    ArrayList<String> results = new ArrayList<>();
    String result = "onResult";
    results.add(result);
    expectedBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, results);

    shadowOf(speechRecognizer).triggerOnResults(expectedBundle);
    shadowOf(getMainLooper()).idle();

    assertThat(listener.bundleReceived).isEqualTo(expectedBundle);
  }

  @Test
  public void onRmsChangedCalled() {
    startListening();

    shadowOf(speechRecognizer).triggerOnRmsChanged(1.0f);

    assertThat(listener.rmsDbReceived).isEqualTo(1.0f);
  }

  @Test
  public void startAndStopListening() {
    startListening();
    shadowOf(speechRecognizer).triggerOnResults(new Bundle());
    speechRecognizer.stopListening();

    assertNoErrorLogs();
  }

  /** Verify the startlistening flow works when using custom component name. */
  @Test
  public void startListeningWithCustomComponent() {
    speechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(
            ApplicationProvider.getApplicationContext(),
            new ComponentName("org.robolectrc", "FakeComponent"));
    speechRecognizer.setRecognitionListener(listener);
    speechRecognizer.startListening(new Intent());
    shadowOf(getMainLooper()).idle();
    shadowOf(speechRecognizer).triggerOnResults(new Bundle());
    assertThat(listener.bundleReceived).isNotNull();

    assertNoErrorLogs();
  }

  @Test
  public void getLatestSpeechRecognizer() {
    SpeechRecognizer newSpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(ApplicationProvider.getApplicationContext());
    newSpeechRecognizer.setRecognitionListener(listener);
    shadowOf(getMainLooper()).idle();
    assertThat(ShadowSpeechRecognizer.getLatestSpeechRecognizer())
        .isSameInstanceAs(newSpeechRecognizer);
  }

  @Test
  public void getLastRecognizerIntent() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.android.test.package");
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
    SpeechRecognizer newSpeechRecognizer =
        SpeechRecognizer.createSpeechRecognizer(ApplicationProvider.getApplicationContext());
    newSpeechRecognizer.setRecognitionListener(listener);
    newSpeechRecognizer.startListening(intent);
    shadowOf(getMainLooper()).idle();
    assertThat(shadowOf(newSpeechRecognizer).getLastRecognizerIntent()).isEqualTo(intent);
  }

  private void startListening() {
    speechRecognizer.setRecognitionListener(listener);
    speechRecognizer.startListening(new Intent());
    shadowOf(getMainLooper()).idle();
  }

  private static void assertNoErrorLogs() {
    for (LogItem item : ShadowLog.getLogsForTag("SpeechRecognizer")) {
      if (item.type >= Log.ERROR) {
        fail("Found unexpected error log: " + item.msg);
      }
    }
  }

  static final class TestRecognitionListener implements RecognitionListener {

    int errorReceived;
    Bundle bundleReceived;
    float rmsDbReceived;
    boolean endofSpeechCalled = false;

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {
      endofSpeechCalled = true;
    }

    @Override
    public void onError(int error) {
      errorReceived = error;
    }

    @Override
    public void onEvent(int eventType, Bundle params) {}

    @Override
    public void onPartialResults(Bundle bundle) {
      bundleReceived = bundle;
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
      bundleReceived = bundle;
    }

    @Override
    public void onResults(Bundle bundle) {
      bundleReceived = bundle;
    }

    @Override
    public void onRmsChanged(float rmsdB) {
      rmsDbReceived = rmsdB;
    }
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void onCreateOnDeviceRecognizer_setsLatestSpeechRecognizer() {
    speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(applicationContext);

    assertThat(speechRecognizer)
        .isSameInstanceAs(ShadowSpeechRecognizer.getLatestSpeechRecognizer());
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setIsOnDeviceRecognitionAvailable_setsAvailability() {
    ShadowSpeechRecognizer.setIsOnDeviceRecognitionAvailable(false);
    assertThat(SpeechRecognizer.isOnDeviceRecognitionAvailable(applicationContext)).isFalse();

    ShadowSpeechRecognizer.setIsOnDeviceRecognitionAvailable(true);
    assertThat(SpeechRecognizer.isOnDeviceRecognitionAvailable(applicationContext)).isTrue();
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void onSupportResultCalled() {
    PausedExecutorService executor = new PausedExecutorService();
    RecognitionSupport recognitionSupport =
        new RecognitionSupport.Builder().addInstalledOnDeviceLanguage("en-US").build();
    speechRecognizer.checkRecognitionSupport(new Intent(), executor, supportCallback);

    ((ShadowSpeechRecognizer) shadowOf(speechRecognizer)).triggerSupportResult(recognitionSupport);
    executor.runAll();

    assertThat(supportCallback.recognitionSupportReceived).isEqualTo(recognitionSupport);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void onSupportErrorCalled() {
    PausedExecutorService executor = new PausedExecutorService();
    TestRecognitionSupportCallback supportCallback = new TestRecognitionSupportCallback();
    speechRecognizer.checkRecognitionSupport(new Intent(), executor, supportCallback);

    ((ShadowSpeechRecognizer) shadowOf(speechRecognizer)).triggerSupportError(1);
    executor.runAll();

    assertThat(supportCallback.errorReceived).isEqualTo(1);
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void triggerModelDownload_setsLatestModelDownloadIntent() {
    Intent modelDownloadIntent = new Intent();
    speechRecognizer.triggerModelDownload(modelDownloadIntent);

    assertThat(((ShadowSpeechRecognizer) shadowOf(speechRecognizer)).getLatestModelDownloadIntent())
        .isSameInstanceAs(modelDownloadIntent);
  }

  static final class TestRecognitionSupportCallback implements RecognitionSupportCallback {

    int errorReceived;
    RecognitionSupport recognitionSupportReceived;

    @Override
    public void onSupportResult(RecognitionSupport recognitionSupport) {
      recognitionSupportReceived = recognitionSupport;
    }

    @Override
    public void onError(int error) {
      errorReceived = error;
    }
  }
}
