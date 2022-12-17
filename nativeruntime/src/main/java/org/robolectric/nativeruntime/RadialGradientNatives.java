package org.robolectric.nativeruntime;

import android.annotation.ColorLong;

/**
 * Native methods for RadialGradient JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/RadialGradient.java
 */
public class RadialGradientNatives {

  public static native long nativeCreate(
      long matrix,
      float startX,
      float startY,
      float startRadius,
      float endX,
      float endY,
      float endRadius,
      @ColorLong long[] colors,
      float[] positions,
      int tileMode,
      long colorSpaceHandle);

  public static native long nativeCreate1(
      long matrix, float x, float y, float radius, int[] colors, float[] positions, int tileMode);

  public static native long nativeCreate2(
      long matrix, float x, float y, float radius, int color0, int color1, int tileMode);

  RadialGradientNatives() {}
}
