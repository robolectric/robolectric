package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;

import android.graphics.ColorSpace;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.ColorSpaceRgbNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeColorSpaceRgb.Picker;

/** Shadow for {@link ColorSpace.Rgb} that is backed by native code */
@Implements(
    value = ColorSpace.Rgb.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativeColorSpaceRgb {

  @Implementation(minSdk = Q)
  protected static long nativeGetNativeFinalizer() {
    return ColorSpaceRgbNatives.nativeGetNativeFinalizer();
  }

  @Implementation(minSdk = Q)
  protected static long nativeCreate(
      float a, float b, float c, float d, float e, float f, float g, float[] xyz) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return ColorSpaceRgbNatives.nativeCreate(a, b, c, d, e, f, g, xyz);
  }

  /** Shadow picker for {@link ColorSpace.Rgb}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowColorSpaceRgb.class, ShadowNativeColorSpaceRgb.class);
    }
  }
}
