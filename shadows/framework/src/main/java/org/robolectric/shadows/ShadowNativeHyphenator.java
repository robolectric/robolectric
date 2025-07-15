package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.text.Hyphenator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.HyphenatorNatives;
import org.robolectric.shadows.ShadowNativeHyphenator.Picker;

/**
 * Shadow for {@link Hyphenator} that is backed by native code. This is a no-op at the moment, as
 * the hyphenation data is not embedded in the android-all jars.
 *
 * <p>There is a single method, {@link Hyphenator#init()}, that is invoked by Zygote. We eventually
 * need to invoke this from {@link org.robolectric.nativeruntime.DefaultNativeRuntimeLoader}.
 */
@Implements(
    value = Hyphenator.class,
    minSdk = P,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeHyphenator {

  @Implementation(minSdk = P, maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nInit() {
    HyphenatorNatives.nInit();
  }

  /** Shadow picker for {@link Hyphenator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeHyphenator.class);
    }
  }
}
