package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(TextToSpeech.class)
public class ShadowTextToSpeech {

  private static final Set<Locale> languageAvailabilities = new HashSet<>();
  private static final Set<Voice> voices = new HashSet<>();
  private static TextToSpeech lastTextToSpeechInstance;

  @RealObject private TextToSpeech tts;

  private Context context;
  private TextToSpeech.OnInitListener listener;
  private String lastSpokenText;
  private boolean shutdown = false;
  private boolean stopped = true;
  private int queueMode = -1;
  private Locale language = null;
  private String lastSynthesizeToFileText;
  private Voice currentVoice = null;

  private final List<String> spokenTextList = new ArrayList<>();

  @Implementation
  protected void __constructor__(
      Context context,
      TextToSpeech.OnInitListener listener,
      String engine,
      String packageName,
      boolean useFallback) {
    this.context = context;
    this.listener = listener;
    lastTextToSpeechInstance = tts;
    Shadow.invokeConstructor(
        TextToSpeech.class,
        tts,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(TextToSpeech.OnInitListener.class, listener),
        ClassParameter.from(String.class, engine),
        ClassParameter.from(String.class, packageName),
        ClassParameter.from(boolean.class, useFallback));
  }

  @Implementation
  protected int initTts() {
    // Has to be overridden because the real code attempts to connect to a non-existent TTS
    // system service.
    return TextToSpeech.SUCCESS;
  }

  /**
   * Speaks the string using the specified queuing strategy and speech parameters.
   *
   * @param params The real implementation converts the hashmap into a bundle, but the bundle
   *     argument is not used in the shadow implementation.
   */
  @Implementation
  protected int speak(
      final String text, final int queueMode, final HashMap<String, String> params) {
    if (RuntimeEnvironment.getApiLevel() >= LOLLIPOP) {
      return reflector(TextToSpeechReflector.class, tts).speak(text, queueMode, params);
    }
    return speak(
        text, queueMode, null, params == null ? null : params.get(Engine.KEY_PARAM_UTTERANCE_ID));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int speak(
      final CharSequence text, final int queueMode, final Bundle params, final String utteranceId) {
    stopped = false;
    lastSpokenText = text.toString();
    spokenTextList.add(text.toString());
    this.queueMode = queueMode;

    if (RuntimeEnvironment.getApiLevel() >= ICE_CREAM_SANDWICH_MR1) {
      if (utteranceId != null) {
        // The onStart and onDone callbacks are normally delivered asynchronously. Since in
        // Robolectric we don't need the wait for TTS package, the asynchronous callbacks are
        // simulated by posting it on a handler. The behavior of the callback can be changed for
        // each individual test by changing the idling mode of the foreground scheduler.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(
            () -> {
              UtteranceProgressListener utteranceProgressListener = getUtteranceProgressListener();
              if (utteranceProgressListener != null) {
                utteranceProgressListener.onStart(utteranceId);
              }
              // The onDone callback is posted in a separate run-loop from onStart, so that tests
              // can pause the scheduler and test the behavior between these two callbacks.
              handler.post(
                  () -> {
                    UtteranceProgressListener utteranceProgressListener2 =
                        getUtteranceProgressListener();
                    if (utteranceProgressListener2 != null) {
                      utteranceProgressListener2.onDone(utteranceId);
                    }
                  });
            });
      }
    }
    return TextToSpeech.SUCCESS;
  }

  @Implementation
  protected void shutdown() {
    shutdown = true;
  }

  @Implementation
  protected int stop() {
    stopped = true;
    return TextToSpeech.SUCCESS;
  }

  @Implementation
  protected int isLanguageAvailable(Locale lang) {
    for (Locale locale : languageAvailabilities) {
      if (locale.getISO3Language().equals(lang.getISO3Language())) {
        if (locale.getISO3Country().equals(lang.getISO3Country())) {
          if (locale.getVariant().equals(lang.getVariant())) {
            return TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
          }
          return TextToSpeech.LANG_COUNTRY_AVAILABLE;
        }
        return TextToSpeech.LANG_AVAILABLE;
      }
    }
    return TextToSpeech.LANG_NOT_SUPPORTED;
  }

  @Implementation
  protected int setLanguage(Locale locale) {
    this.language = locale;
    return isLanguageAvailable(locale);
  }

  /**
   * Stores {@code text} and returns {@link TextToSpeech#SUCCESS}.
   *
   * @see #getLastSynthesizeToFileText()
   */
  @Implementation(minSdk = LOLLIPOP)
  protected int synthesizeToFile(CharSequence text, Bundle params, File file, String utteranceId) {
    this.lastSynthesizeToFileText = text.toString();
    return TextToSpeech.SUCCESS;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected int setVoice(Voice voice) {
    this.currentVoice = voice;
    return TextToSpeech.SUCCESS;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected Set<Voice> getVoices() {
    return voices;
  }

  public UtteranceProgressListener getUtteranceProgressListener() {
    return ReflectionHelpers.getField(tts, "mUtteranceProgressListener");
  }

  public Context getContext() {
    return context;
  }

  public TextToSpeech.OnInitListener getOnInitListener() {
    return listener;
  }

  public String getLastSpokenText() {
    return lastSpokenText;
  }

  public void clearLastSpokenText() {
    lastSpokenText = null;
  }

  public boolean isShutdown() {
    return shutdown;
  }

  /** @return {@code true} if the TTS is stopped. */
  public boolean isStopped() {
    return stopped;
  }

  public int getQueueMode() {
    return queueMode;
  }

  /**
   * Returns {@link Locale} set using {@link TextToSpeech#setLanguage(Locale)} or null if not set.
   */
  public Locale getCurrentLanguage() {
    return language;
  }

  /**
   * Returns last text {@link CharSequence} passed to {@link
   * TextToSpeech#synthesizeToFile(CharSequence, Bundle, File, String)}.
   */
  public String getLastSynthesizeToFileText() {
    return lastSynthesizeToFileText;
  }

  /** Returns list of all the text spoken by {@link #speak}. */
  public ImmutableList<String> getSpokenTextList() {
    return ImmutableList.copyOf(spokenTextList);
  }

  /**
   * Makes {@link Locale} an available language returned by {@link
   * TextToSpeech#isLanguageAvailable(Locale)}. The value returned by {@link
   * #isLanguageAvailable(Locale)} will vary depending on language, country, and variant.
   */
  public static void addLanguageAvailability(Locale locale) {
    languageAvailabilities.add(locale);
  }

  /** Makes {@link Voice} an available voice returned by {@link TextToSpeech#getVoices()}. */
  public static void addVoice(Voice voice) {
    voices.add(voice);
  }

  /** Returns {@link Voice} set using {@link TextToSpeech#setVoice(Voice)}, or null if not set. */
  public Voice getCurrentVoice() {
    return currentVoice;
  }

  /** Returns the most recently instantiated {@link TextToSpeech} or null if none exist. */
  public static TextToSpeech getLastTextToSpeechInstance() {
    return lastTextToSpeechInstance;
  }

  @Resetter
  public static void reset() {
    languageAvailabilities.clear();
    voices.clear();
    lastTextToSpeechInstance = null;
  }

  @ForType(TextToSpeech.class)
  interface TextToSpeechReflector {

    @Direct
    int speak(final String text, final int queueMode, final HashMap params);
  }
}
