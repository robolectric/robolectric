package org.robolectric.nativeruntime;

/**
 * Native methods for PathMeasure JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/PathMeasure.java
 */
public final class PathMeasureNatives {

  public static native long native_create(long nativePath, boolean forceClosed);

  public static native void native_setPath(
      long nativeInstance, long nativePath, boolean forceClosed);

  public static native float native_getLength(long nativeInstance);

  public static native boolean native_getPosTan(
      long nativeInstance, float distance, float[] pos, float[] tan);

  public static native boolean native_getMatrix(
      long nativeInstance, float distance, long nativeMatrix, int flags);

  public static native boolean native_getSegment(
      long nativeInstance, float startD, float stopD, long nativePath, boolean startWithMoveTo);

  public static native boolean native_isClosed(long nativeInstance);

  public static native boolean native_nextContour(long nativeInstance);

  public static native void native_destroy(long nativeInstance);

  private PathMeasureNatives() {}
}
