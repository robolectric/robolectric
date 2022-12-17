package org.robolectric.nativeruntime;

/**
 * Native methods for LinearGradient JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/LinearGradient.java
 */
public final class LinearGradientNatives {
  public static native long nativeCreate(
      long matrix,
      float x0,
      float y0,
      float x1,
      float y1,
      long[] colors,
      float[] positions,
      int tileMode,
      long colorSpaceHandle);

  public static native long nativeCreate1(
      long matrix,
      float x0,
      float y0,
      float x1,
      float y1,
      int[] colors,
      float[] positions,
      int tileMode);

  public static native long nativeCreate2(
      long matrix, float x0, float y0, float x1, float y1, int color0, int color1, int tileMode);

  private LinearGradientNatives() {}
}
