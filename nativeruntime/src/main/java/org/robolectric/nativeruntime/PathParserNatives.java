package org.robolectric.nativeruntime;

/**
 * Native methods for PathParser JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/PathParser.java
 */
public final class PathParserNatives {

  public static native void nParseStringForPath(long pathPtr, String pathString, int stringLength);

  public static native long nCreatePathDataFromString(String pathString, int stringLength);

  public static native void nCreatePathFromPathData(long outPathPtr, long pathData);

  public static native long nCreateEmptyPathData();

  public static native long nCreatePathData(long nativePtr);

  public static native boolean nInterpolatePathData(
      long outDataPtr, long fromDataPtr, long toDataPtr, float fraction);

  public static native void nFinalize(long nativePtr);

  public static native boolean nCanMorph(long fromDataPtr, long toDataPtr);

  public static native void nSetPathData(long outDataPtr, long fromDataPtr);

  private PathParserNatives() {}
}
