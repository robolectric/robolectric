package org.robolectric.nativeruntime;

/**
 * Native methods for PathDashPathEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/PathDashPathEffect.java
 */
public final class PathDashPathEffectNatives {

  public static native long nativeCreate(
      long nativePath, float advance, float phase, int nativeStyle);

  private PathDashPathEffectNatives() {}
}
