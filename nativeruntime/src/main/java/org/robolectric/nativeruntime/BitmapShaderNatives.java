package org.robolectric.nativeruntime;

/**
 * Native methods for BitmapShader JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/BitmapShader.java
 */
public final class BitmapShaderNatives {

  public static native long nativeCreate(
      long nativeMatrix,
      long bitmapHandle,
      int shaderTileModeX,
      int shaderTileModeY,
      boolean filter);

  private BitmapShaderNatives() {}
}
