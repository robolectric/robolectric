package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.LightingColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.LightingColorFilterNatives;
import org.robolectric.shadows.ShadowNativeLightingColorFilter.Picker;

/** Shadow for {@link LightingColorFilter} that is backed by native code */
@Implements(value = LightingColorFilter.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeLightingColorFilter {

  @Implementation(minSdk = O)
  protected static long native_CreateLightingFilter(int mul, int add) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return LightingColorFilterNatives.native_CreateLightingFilter(mul, add);
  }

  /** Shadow picker for {@link LightingColorFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeLightingColorFilter.class);
    }
  }
}
