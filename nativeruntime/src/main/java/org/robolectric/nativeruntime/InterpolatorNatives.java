package org.robolectric.nativeruntime;

/**
 * Native methods for Interpolator JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Interpolator.java
 */
public final class InterpolatorNatives {
  public static native long nativeConstructor(int valueCount, int frameCount);

  public static native void nativeDestructor(long nativeInstance);

  public static native void nativeReset(long nativeInstance, int valueCount, int frameCount);

  public static native void nativeSetKeyFrame(
      long nativeInstance, int index, int msec, float[] values, float[] blend);

  public static native void nativeSetRepeatMirror(
      long nativeInstance, float repeatCount, boolean mirror);

  public static native int nativeTimeToValues(long nativeInstance, int msec, float[] values);

  private InterpolatorNatives() {}
}
