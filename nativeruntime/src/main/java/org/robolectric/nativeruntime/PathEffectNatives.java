package org.robolectric.nativeruntime;

/**
 * Native methods for PathEffect JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/PathEffect.java
 */
public final class PathEffectNatives {

  public static native void nativeDestructor(long nativePatheffect);

  private PathEffectNatives() {}
}
