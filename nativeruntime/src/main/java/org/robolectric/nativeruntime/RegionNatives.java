package org.robolectric.nativeruntime;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Parcel;

/**
 * Native methods for Region JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/Region.java
 */
public final class RegionNatives {

  // Must be this style to match AOSP branch
  public long mNativeRegion;

  public static native boolean nativeEquals(long nativeR1, long nativeR2);

  public static native long nativeConstructor();

  public static native void nativeDestructor(long nativeRegion);

  public static native void nativeSetRegion(long nativeDst, long nativeSrc);

  public static native boolean nativeSetRect(
      long nativeDst, int left, int top, int right, int bottom);

  public static native boolean nativeSetPath(long nativeDst, long nativePath, long nativeClip);

  public static native boolean nativeGetBounds(long nativeRegion, Rect rect);

  public static native boolean nativeGetBoundaryPath(long nativeRegion, long nativePath);

  public static native boolean nativeOp(
      long nativeDst, int left, int top, int right, int bottom, int op);

  public static native boolean nativeOp(long nativeDst, Rect rect, long nativeRegion, int op);

  public static native boolean nativeOp(
      long nativeDst, long nativeRegion1, long nativeRegion2, int op);

  public static native long nativeCreateFromParcel(Parcel p);

  public static native boolean nativeWriteToParcel(long nativeRegion, Parcel p);

  public static native String nativeToString(long nativeRegion);

  public native boolean isEmpty();

  public native boolean isRect();

  public native boolean isComplex();

  public native boolean contains(int x, int y);

  public native boolean quickContains(int left, int top, int right, int bottom);

  public native boolean quickReject(int left, int top, int right, int bottom);

  public native boolean quickReject(Region rgn);

  public native void translate(int dx, int dy, Region dst);

  public native void scale(float scale, Region dst);
}
