package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.speech.SpeechRecognizer;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.V;

/**
 * Robolectric shadow for SpeechRecognizerProxy.
 *
 * <p>Prior to Android V, SpeechRecognizer contained all functionality within one class. The {@link
 * ShadowSpeechRecognizer} shadow would work correctly to shadow both static and instance functions
 * of the class. With Android V, the instance of {@link SpeechRecognizer} returned from {@link
 * SpeechRecognizer#createSpeechRecognizer(Context)} is an instance of {@link
 * android.speech.SpeechRecognizerProxy} which delegates all calls to {@link
 * android.speech.SpeechRecognizerImpl}.
 *
 * <p>This shadow (for the proxy subclass) works in coordination with {@link
 * ShadowSpeechRecognizerImpl} to ensure that the functionality of {@link ShadowSpeechRecognizer}
 * still works in tests prior to Android V and on Android V+.
 *
 * <p>Customizations for this implementation:
 *
 * <ul>
 *   <li>Does not intercept any public API since all calls will be placed to {@link
 *       android.speech.SpeechRecognizerImpl}
 *   <li>Instead of returning its own state, returns the state of the delegate
 *       (ShadowSpeechRecognizerImpl)
 *   <li>No change in direct accessor since it is not required
 * </ul>
 */
@Implements(
    className = ShadowSpeechRecognizerProxy.CLASS_NAME,
    isInAndroidSdk = false,
    minSdk = V.SDK_INT)
public class ShadowSpeechRecognizerProxy extends ShadowSpeechRecognizer {
  protected static final String CLASS_NAME = "android.speech.SpeechRecognizerProxy";

  @RealObject SpeechRecognizer realSpeechRecognizer;

  /**
   * The proxy and its shadow do not store their own state and this shadow does not intercept any of
   * the relevant functions on its shadowed class. Rather, the delegate within the proxy has the
   * correct state and intercepts such calls.
   */
  @Override
  protected ShadowSpeechRecognizerState getState() {
    SpeechRecognizer delegate =
        reflector(SpeechRecognizerProxyReflector.class, realSpeechRecognizer).getDelegate();
    ShadowSpeechRecognizerImpl delegateShadow = Shadow.extract(delegate);
    return delegateShadow.internalGetState();
  }

  /** Reflector interface for {@link SpeechRecognizerProxy}'s internals. */
  @ForType(className = CLASS_NAME)
  interface SpeechRecognizerProxyReflector {
    @Accessor("mDelegate")
    SpeechRecognizer getDelegate();
  }
}
