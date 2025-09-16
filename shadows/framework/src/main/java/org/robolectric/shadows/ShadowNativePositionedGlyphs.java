package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.text.PositionedGlyphs;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PositionedGlyphsNatives;
import org.robolectric.shadows.ShadowNativePositionedGlyphs.Picker;

/** Shadow for {@link PositionedGlyphs} that is backed by native code */
@Implements(
    value = PositionedGlyphs.class,
    minSdk = S,
    shadowPicker = Picker.class,
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativePositionedGlyphs {
  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetGlyphCount(long minikinLayout) {
    return PositionedGlyphsNatives.nGetGlyphCount(minikinLayout);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetTotalAdvance(long minikinLayout) {
    return PositionedGlyphsNatives.nGetTotalAdvance(minikinLayout);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetAscent(long minikinLayout) {
    return PositionedGlyphsNatives.nGetAscent(minikinLayout);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetDescent(long minikinLayout) {
    return PositionedGlyphsNatives.nGetDescent(minikinLayout);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetGlyphId(long minikinLayout, int i) {
    return PositionedGlyphsNatives.nGetGlyphId(minikinLayout, i);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetX(long minikinLayout, int i) {
    return PositionedGlyphsNatives.nGetX(minikinLayout, i);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetY(long minikinLayout, int i) {
    return PositionedGlyphsNatives.nGetY(minikinLayout, i);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nGetFont(long minikinLayout, int i) {
    return PositionedGlyphsNatives.nGetFont(minikinLayout, i);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nReleaseFunc() {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PositionedGlyphsNatives.nReleaseFunc();
  }

  /** Shadow picker for {@link PositionedGlyphs}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativePositionedGlyphs.class);
    }
  }
}
