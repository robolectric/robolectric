package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.BlurMaskFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.BlurMaskFilterNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeBlurMaskFilter.Picker;

/** Shadow for {@link BlurMaskFilter} that is backed by native code */
@Implements(value = BlurMaskFilter.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeBlurMaskFilter {

  @Implementation(minSdk = O)
  protected static long nativeConstructor(float radius, int style) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return BlurMaskFilterNatives.nativeConstructor(radius, style);
  }

  /** Shadow picker for {@link BlurMaskFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeBlurMaskFilter.class);
    }
  }
}
