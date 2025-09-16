package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.BlendModeColorFilter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.BlendModeColorFilterNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeBlendModeColorFilter.Picker;

/** Shadow for {@link BlendModeColorFilter} that is backed by native code */
@Implements(
    value = BlendModeColorFilter.class,
    minSdk = Q,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeBlendModeColorFilter {

  @Implementation(minSdk = Q, maxSdk = UPSIDE_DOWN_CAKE)
  protected static long native_CreateBlendModeFilter(int srcColor, int blendmode) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return BlendModeColorFilterNatives.native_CreateBlendModeFilter(srcColor, blendmode);
  }

  /** Shadow picker for {@link BlendModeColorFilter}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeBlendModeColorFilter.class);
    }
  }
}
