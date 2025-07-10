package org.robolectric.shadows;

import android.text.Hyphenator;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowNativeHyphenator.Picker;
import org.robolectric.versioning.AndroidVersions.V;

/**
 * Shadow for {@link Hyphenator} that is backed by native code. This is a no-op at the moment, as
 * the hyphenation data is not embedded in the android-all jars.
 *
 * <p>There is a single method, {@link Hyphenator#init()}, that is invoked by Zygote. We eventually
 * need to invoke this from {@link org.robolectric.nativeruntime.DefaultNativeRuntimeLoader}.
 */
@Implements(
    value = Hyphenator.class,
    minSdk = V.SDK_INT,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeHyphenator {

  /** Shadow picker for {@link Hyphenator}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeHyphenator.class);
    }
  }
}
