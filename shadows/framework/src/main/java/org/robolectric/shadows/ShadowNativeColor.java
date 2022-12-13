package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Color;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.ColorNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadows.ShadowNativeColor.Picker;

/** Shadow for {@link Color} that is backed by native code */
@Implements(value = Color.class, minSdk = O, shadowPicker = Picker.class, isInAndroidSdk = false)
public class ShadowNativeColor {

  @Implementation(minSdk = O)
  protected static void nativeRGBToHSV(int red, int greed, int blue, float[] hsv) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    ColorNatives.nativeRGBToHSV(red, greed, blue, hsv);
  }

  @Implementation(minSdk = O)
  protected static int nativeHSVToColor(int alpha, float[] hsv) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return ColorNatives.nativeHSVToColor(alpha, hsv);
  }

  /** Shadow picker for {@link Color}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowColor.class, ShadowNativeColor.class);
    }
  }
}
