package org.robolectric.nativeruntime;

import android.graphics.Rect;

/**
 * Native methods for RegionIterator JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/RegionIterator.java
 */
public final class RegionIteratorNatives {

  public static native long nativeConstructor(long nativeRegion);

  public static native void nativeDestructor(long nativeIter);

  public static native boolean nativeNext(long nativeIter, Rect r);

  private RegionIteratorNatives() {}
}
