package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;

import android.graphics.RenderEffect;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RenderEffectNatives;
import org.robolectric.shadows.ShadowNativeRenderEffect.Picker;

/** Shadow for {@link RenderEffect} that is backed by native code */
@Implements(value = RenderEffect.class, minSdk = O, shadowPicker = Picker.class)
public class ShadowNativeRenderEffect {
  static {
    DefaultNativeRuntimeLoader.injectAndLoad();
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateOffsetEffect(float offsetX, float offsetY, long nativeInput) {
    return RenderEffectNatives.nativeCreateOffsetEffect(offsetX, offsetY, nativeInput);
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateBlurEffect(
      float radiusX, float radiusY, long nativeInput, int edgeTreatment) {
    return RenderEffectNatives.nativeCreateBlurEffect(radiusX, radiusY, nativeInput, edgeTreatment);
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateBitmapEffect(
      long bitmapHandle,
      float srcLeft,
      float srcTop,
      float srcRight,
      float srcBottom,
      float dstLeft,
      float dstTop,
      float dstRight,
      float dstBottom) {
    return RenderEffectNatives.nativeCreateBitmapEffect(
        bitmapHandle, srcLeft, srcTop, srcRight, srcBottom, dstLeft, dstTop, dstRight, dstBottom);
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateColorFilterEffect(long colorFilter, long nativeInput) {
    return RenderEffectNatives.nativeCreateColorFilterEffect(colorFilter, nativeInput);
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateBlendModeEffect(long dst, long src, int blendmode) {
    return RenderEffectNatives.nativeCreateBlendModeEffect(dst, src, blendmode);
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateChainEffect(long outer, long inner) {
    return RenderEffectNatives.nativeCreateChainEffect(outer, inner);
  }

  @Implementation(minSdk = S)
  protected static long nativeCreateShaderEffect(long shader) {
    return RenderEffectNatives.nativeCreateShaderEffect(shader);
  }

  @Implementation(minSdk = S)
  protected static long nativeGetFinalizer() {
    return RenderEffectNatives.nativeGetFinalizer();
  }

  /** Shadow picker for {@link RenderEffect}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(null, ShadowNativeRenderEffect.class);
    }
  }
}
