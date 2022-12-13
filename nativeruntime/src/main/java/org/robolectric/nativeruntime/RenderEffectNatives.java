package org.robolectric.nativeruntime;

/**
 * Native methods for RenderEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/RenderEffect.java
 */
public final class RenderEffectNatives {

  public static native long nativeCreateOffsetEffect(
      float offsetX, float offsetY, long nativeInput);

  public static native long nativeCreateBlurEffect(
      float radiusX, float radiusY, long nativeInput, int edgeTreatment);

  public static native long nativeCreateBitmapEffect(
      long bitmapHandle,
      float srcLeft,
      float srcTop,
      float srcRight,
      float srcBottom,
      float dstLeft,
      float dstTop,
      float dstRight,
      float dstBottom);

  public static native long nativeCreateColorFilterEffect(long colorFilter, long nativeInput);

  public static native long nativeCreateBlendModeEffect(long dst, long src, int blendmode);

  public static native long nativeCreateChainEffect(long outer, long inner);

  public static native long nativeCreateShaderEffect(long shader);

  public static native long nativeGetFinalizer();

  private RenderEffectNatives() {}
}
