package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowTextToSpeechTest {
  private Activity activity;

  @Before
  public void setUp() {
    activity = Robolectric.buildActivity(Activity.class).create().get();
  }

  @Test
  public void shouldNotBeNull() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(textToSpeech).isNotNull();
    assertThat(shadowOf(textToSpeech)).isNotNull();
  }

  @Test
  public void onInitListener_success_getsCalledAsynchronously() {
    AtomicReference<Integer> onInitCalled = new AtomicReference<>();
    TextToSpeech.OnInitListener listener = onInitCalled::set;
    TextToSpeech textToSpeech = new TextToSpeech(activity, listener);
    assertThat(textToSpeech).isNotNull();
    Shadows.shadowOf(textToSpeech).getOnInitListener().onInit(TextToSpeech.SUCCESS);
    assertThat(onInitCalled.get()).isEqualTo(TextToSpeech.SUCCESS);
  }

  @Test
  public void onInitListener_error() {
    AtomicReference<Integer> onInitCalled = new AtomicReference<>();
    TextToSpeech.OnInitListener listener = onInitCalled::set;
    TextToSpeech textToSpeech = new TextToSpeech(activity, listener);
    assertThat(textToSpeech).isNotNull();
    Shadows.shadowOf(textToSpeech).getOnInitListener().onInit(TextToSpeech.ERROR);
    assertThat(onInitCalled.get()).isEqualTo(TextToSpeech.ERROR);
  }

  @Test
  public void getContext_shouldReturnContext() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(shadowOf(textToSpeech).getContext()).isEqualTo(activity);
  }

  @Test
  public void getOnInitListener_shouldReturnListener() {
    TextToSpeech.OnInitListener listener = result -> {};
    TextToSpeech textToSpeech = new TextToSpeech(activity, listener);
    assertThat(shadowOf(textToSpeech).getOnInitListener()).isEqualTo(listener);
  }

  @Test
  public void getLastSpokenText_shouldReturnSpokenText() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isEqualTo("Hello");
  }

  @Test
  public void getLastSpokenText_shouldReturnMostRecentText() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    textToSpeech.speak("Hi", TextToSpeech.QUEUE_FLUSH, null);
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isEqualTo("Hi");
  }

  @Test
  public void clearLastSpokenText_shouldSetLastSpokenTextToNull() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    shadowOf(textToSpeech).clearLastSpokenText();
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isNull();
  }

  @Test
  public void isShutdown_shouldReturnFalseBeforeShutdown() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(shadowOf(textToSpeech).isShutdown()).isFalse();
  }

  @Test
  public void isShutdown_shouldReturnTrueAfterShutdown() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.shutdown();
    assertThat(shadowOf(textToSpeech).isShutdown()).isTrue();
  }

  @Test
  public void isStopped_shouldReturnTrueBeforeSpeak() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(shadowOf(textToSpeech).isStopped()).isTrue();
  }

  @Test
  public void isStopped_shouldReturnTrueAfterStop() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.stop();
    assertThat(shadowOf(textToSpeech).isStopped()).isTrue();
  }

  @Test
  public void isStopped_shouldReturnFalseAfterSpeak() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);
    assertThat(shadowOf(textToSpeech).isStopped()).isFalse();
  }

  @Test
  public void getQueueMode_shouldReturnMostRecentQueueMode() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_ADD, null);
    assertThat(shadowOf(textToSpeech).getQueueMode()).isEqualTo(TextToSpeech.QUEUE_ADD);
  }

  @Test
  public void threeArgumentSpeak_withUtteranceId_shouldGetCallbackUtteranceId() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    HashMap<String, String> paramsMap = new HashMap<>();
    paramsMap.put(Engine.KEY_PARAM_UTTERANCE_ID, "ThreeArgument");
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, paramsMap);

    shadowMainLooper().idle();

    verify(mockListener).onStart("ThreeArgument");
    verify(mockListener).onDone("ThreeArgument");
  }

  @Test
  public void threeArgumentSpeak_withoutUtteranceId_shouldDoesNotGetCallback() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null);

    shadowMainLooper().idle();

    verify(mockListener, never()).onStart(null);
    verify(mockListener, never()).onDone(null);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void speak_withUtteranceId_shouldReturnSpokenText() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null, "TTSEnable");
    assertThat(shadowOf(textToSpeech).getLastSpokenText()).isEqualTo("Hello");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void onUtteranceProgressListener_shouldGetCallbackUtteranceId() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    textToSpeech.speak("Hello", TextToSpeech.QUEUE_FLUSH, null, "TTSEnable");

    shadowMainLooper().idle();

    verify(mockListener).onStart("TTSEnable");
    verify(mockListener).onDone("TTSEnable");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_lastSynthesizeToFileTextStored() throws IOException {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    Bundle bundle = new Bundle();
    File file = createFile("example.txt");
    int result = textToSpeech.synthesizeToFile("text", bundle, file, "id");

    assertThat(result).isEqualTo(TextToSpeech.SUCCESS);
    assertThat(shadowOf(textToSpeech).getLastSynthesizeToFileText()).isEqualTo("text");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_byDefault_doesNotCallOnStart() throws IOException {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    Bundle bundle = new Bundle();
    File file = createFile("example.txt");

    textToSpeech.synthesizeToFile("text", bundle, file, "id");

    verify(mockListener, never()).onDone("id");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_byDefault_doesNotCallOnDone() throws IOException {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    Bundle bundle = new Bundle();
    File file = createFile("example.txt");

    textToSpeech.synthesizeToFile("text", bundle, file, "id");

    verify(mockListener, never()).onDone("id");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_successSimulated_callsOnStart() throws IOException {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    Bundle bundle = new Bundle();
    File file = createFile("example.txt");

    ShadowTextToSpeech shadowTextToSpeech = shadowOf(textToSpeech);
    shadowTextToSpeech.simulateSynthesizeToFileResult(TextToSpeech.SUCCESS);

    textToSpeech.synthesizeToFile("text", bundle, file, "id");

    verify(mockListener).onStart("id");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_successSimulated_callsOnDone() throws IOException {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    Bundle bundle = new Bundle();
    File file = createFile("example.txt");

    ShadowTextToSpeech shadowTextToSpeech = shadowOf(textToSpeech);
    shadowTextToSpeech.simulateSynthesizeToFileResult(TextToSpeech.SUCCESS);

    textToSpeech.synthesizeToFile("text", bundle, file, "id");

    verify(mockListener).onDone("id");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_setToFail_doesNotCallIsDone() throws IOException {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    Bundle bundle = new Bundle();
    File file = createFile("example.txt");

    ShadowTextToSpeech shadowTextToSpeech = shadowOf(textToSpeech);
    // The actual error used does not matter for this test.
    shadowTextToSpeech.simulateSynthesizeToFileResult(TextToSpeech.ERROR_NETWORK_TIMEOUT);

    textToSpeech.synthesizeToFile("text", bundle, file, "id");

    verify(mockListener, never()).onDone("id");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_setToFail_callsOnErrorWithErrorCode() throws IOException {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    UtteranceProgressListener mockListener = mock(UtteranceProgressListener.class);
    textToSpeech.setOnUtteranceProgressListener(mockListener);
    Bundle bundle = new Bundle();
    File file = createFile("example.txt");

    ShadowTextToSpeech shadowTextToSpeech = shadowOf(textToSpeech);
    int errorCode = TextToSpeech.ERROR_NETWORK_TIMEOUT;
    shadowTextToSpeech.simulateSynthesizeToFileResult(errorCode);

    textToSpeech.synthesizeToFile("text", bundle, file, "id");

    verify(mockListener).onError("id", errorCode);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void synthesizeToFile_neverCalled_lastSynthesizeToFileTextNull() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(shadowOf(textToSpeech).getLastSynthesizeToFileText()).isNull();
  }

  @Test
  public void getCurrentLanguage_languageSet_returnsLanguage() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    Locale language = Locale.forLanguageTag("pl-pl");
    textToSpeech.setLanguage(language);
    assertThat(shadowOf(textToSpeech).getCurrentLanguage()).isEqualTo(language);
  }

  @Test
  public void getCurrentLanguage_languageNeverSet_returnsNull() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(shadowOf(textToSpeech).getCurrentLanguage()).isNull();
  }

  @Test
  public void isLanguageAvailable_neverAdded_returnsUnsupported() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(
            textToSpeech.isLanguageAvailable(
                new Locale.Builder().setLanguage("pl").setRegion("pl").build()))
        .isEqualTo(TextToSpeech.LANG_NOT_SUPPORTED);
  }

  @Test
  public void isLanguageAvailable_twoLanguageAvailabilities_returnsRequestedAvailability() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    ShadowTextToSpeech.addLanguageAvailability(
        new Locale.Builder().setLanguage("pl").setRegion("pl").build());
    ShadowTextToSpeech.addLanguageAvailability(
        new Locale.Builder().setLanguage("ja").setRegion("jp").build());

    assertThat(
            textToSpeech.isLanguageAvailable(
                new Locale.Builder().setLanguage("pl").setRegion("pl").build()))
        .isEqualTo(TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE);
  }

  @Test
  public void isLanguageAvailable_matchingVariant_returnsCountryVarAvailable() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    ShadowTextToSpeech.addLanguageAvailability(
        new Locale.Builder().setLanguage("en").setRegion("us").setVariant("WOLTK").build());

    assertThat(
            textToSpeech.isLanguageAvailable(
                new Locale.Builder().setLanguage("en").setRegion("us").setVariant("WOLTK").build()))
        .isEqualTo(TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE);
  }

  @Test
  public void isLanguageAvailable_matchingCountry_returnsLangCountryAvailable() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    ShadowTextToSpeech.addLanguageAvailability(
        new Locale.Builder().setLanguage("en").setRegion("us").setVariant("ONETW").build());

    assertThat(
            textToSpeech.isLanguageAvailable(
                new Locale.Builder().setLanguage("en").setRegion("us").setVariant("THREE").build()))
        .isEqualTo(TextToSpeech.LANG_COUNTRY_AVAILABLE);
  }

  @Test
  public void isLanguageAvailable_matchingLanguage_returnsLangAvailable() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    ShadowTextToSpeech.addLanguageAvailability(
        new Locale.Builder().setLanguage("en").setRegion("us").build());

    assertThat(
            textToSpeech.isLanguageAvailable(
                new Locale.Builder().setLanguage("en").setRegion("gb").build()))
        .isEqualTo(TextToSpeech.LANG_AVAILABLE);
  }

  @Test
  public void isLanguageAvailable_matchingNone_returnsLangNotSupported() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    ShadowTextToSpeech.addLanguageAvailability(
        new Locale.Builder().setLanguage("en").setRegion("us").build());

    assertThat(
            textToSpeech.isLanguageAvailable(
                new Locale.Builder().setLanguage("ja").setRegion("jp").build()))
        .isEqualTo(TextToSpeech.LANG_NOT_SUPPORTED);
  }

  @Test
  public void getLastTextToSpeechInstance_neverConstructed_returnsNull() {
    assertThat(ShadowTextToSpeech.getLastTextToSpeechInstance()).isNull();
  }

  @Test
  public void getLastTextToSpeechInstance_constructed_returnsInstance() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(ShadowTextToSpeech.getLastTextToSpeechInstance()).isEqualTo(textToSpeech);
  }

  @Test
  public void getLastTextToSpeechInstance_constructedTwice_returnsMostRecentInstance() {
    TextToSpeech textToSpeechOne = new TextToSpeech(activity, result -> {});
    TextToSpeech textToSpeechTwo = new TextToSpeech(activity, result -> {});

    assertThat(ShadowTextToSpeech.getLastTextToSpeechInstance()).isEqualTo(textToSpeechTwo);
    assertThat(ShadowTextToSpeech.getLastTextToSpeechInstance()).isNotEqualTo(textToSpeechOne);
  }

  @Test
  public void getSpokenTextList_neverSpoke_returnsEmpty() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});
    assertThat(shadowOf(textToSpeech).getSpokenTextList()).isEmpty();
  }

  @Test
  public void getSpokenTextList_spoke_returnsSpokenTexts() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});

    textToSpeech.speak("one", TextToSpeech.QUEUE_FLUSH, null);
    textToSpeech.speak("two", TextToSpeech.QUEUE_FLUSH, null);
    textToSpeech.speak("three", TextToSpeech.QUEUE_FLUSH, null);

    assertThat(shadowOf(textToSpeech).getSpokenTextList()).containsExactly("one", "two", "three");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getCurrentVoice_voiceSet_returnsVoice() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});

    Voice voice =
        new Voice(
            "test voice",
            Locale.getDefault(),
            Voice.QUALITY_VERY_HIGH,
            Voice.LATENCY_LOW,
            false /* requiresNetworkConnection */,
            ImmutableSet.of());
    textToSpeech.setVoice(voice);

    assertThat(shadowOf(textToSpeech).getCurrentVoice()).isEqualTo(voice);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getVoices_returnsAvailableVoices() {
    TextToSpeech textToSpeech = new TextToSpeech(activity, result -> {});

    Voice voice =
        new Voice(
            "test voice",
            Locale.getDefault(),
            Voice.QUALITY_VERY_HIGH,
            Voice.LATENCY_LOW,
            false /* requiresNetworkConnection */,
            ImmutableSet.of());
    ShadowTextToSpeech.addVoice(voice);

    assertThat(shadowOf(textToSpeech).getVoices()).containsExactly(voice);
  }

  private static File createFile(String filename) throws IOException {
    TemporaryFolder temporaryFolder = new TemporaryFolder();
    temporaryFolder.create();
    return temporaryFolder.newFile(filename);
  }
}
