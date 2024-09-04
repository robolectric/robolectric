package org.robolectric.nativeruntime;

/**
 * Native methods for PathIterator JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-14.0.0_r1:frameworks/base/graphics/java/android/graphics/PathIterator.java
 */
public final class PathIteratorNatives {

  public static native long nCreate(long nativePath);

  public static native long nGetFinalizer();

  public static native int nNext(long nativeIterator, long pointsAddress);

  public static native int nPeek(long nativeIterator);

  private PathIteratorNatives() {}
}
