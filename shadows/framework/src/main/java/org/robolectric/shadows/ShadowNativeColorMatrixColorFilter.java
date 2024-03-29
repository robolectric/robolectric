package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.ColorMatrixColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.ColorMatrixColorFilterNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeColorMatrixColorFilter.Picker;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link ColorMatrixColorFilter} that is backed by native code */
@Implements(
    value = ColorMatrixColorFilter.class,
    minSdk = O,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeColorMatrixColorFilter {

  @Implementation(minSdk = O, maxSdk = U.SDK_INT)
  protected static long nativeColorMatrixFilter(float[] array) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return ColorMatrixColorFilterNatives.nativeColorMatrixFilter(array);
  }

  /** Shadow picker for {@link ColorMatrixColorFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeColorMatrixColorFilter.class);
    }
  }
}
