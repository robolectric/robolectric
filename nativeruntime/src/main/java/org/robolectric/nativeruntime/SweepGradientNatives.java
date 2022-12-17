package org.robolectric.nativeruntime;

/**
 * Native methods for SweepGradient JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/SweepGradient.java
 */
public class SweepGradientNatives {

  public static native long nativeCreate(
      long matrix, float x, float y, long[] colors, float[] positions, long colorSpaceHandle);

  public static native long nativeCreate1(
      long matrix, float x, float y, int[] colors, float[] positions);

  public static native long nativeCreate2(long matrix, float x, float y, int color0, int color1);

  private SweepGradientNatives() {}
}
