package org.robolectric.nativeruntime;

/**
 * Native methods for NativeInterpolatorFactory JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/NativeInterpolatorFactory.java
 */
public final class NativeInterpolatorFactoryNatives {

  public static native long createAccelerateDecelerateInterpolator();

  public static native long createAccelerateInterpolator(float factor);

  public static native long createAnticipateInterpolator(float tension);

  public static native long createAnticipateOvershootInterpolator(float tension);

  public static native long createBounceInterpolator();

  public static native long createCycleInterpolator(float cycles);

  public static native long createDecelerateInterpolator(float factor);

  public static native long createLinearInterpolator();

  public static native long createOvershootInterpolator(float tension);

  public static native long createPathInterpolator(float[] x, float[] y);

  public static native long createLutInterpolator(float[] values);

  private NativeInterpolatorFactoryNatives() {}
}
