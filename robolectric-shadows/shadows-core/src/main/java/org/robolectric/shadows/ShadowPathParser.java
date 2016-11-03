package org.robolectric.shadows;

import android.util.PathParser;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static android.os.Build.VERSION_CODES.N;

@Implements(value = PathParser.class, minSdk = N)
public class ShadowPathParser {
//  private static native void nParseStringForPath(long pathPtr, String pathString,
//                                                 int stringLength);
//  private static native void nCreatePathFromPathData(long outPathPtr, long pathData);
//  private static native long nCreateEmptyPathData();
//  private static native long nCreatePathData(long nativePtr);

  @Implementation
  public static long nCreatePathDataFromString(String pathString, int stringLength) {
    return 1;
  }

  @Implementation
  public static boolean nInterpolatePathData(long outDataPtr, long fromDataPtr,
                                             long toDataPtr, float fraction) {
    return true;
  }

//  private static native void nFinalize(long nativePtr);

  @Implementation
  public static boolean nCanMorph(long fromDataPtr, long toDataPtr) {
    return true;
  }
//  private static native void nSetPathData(long outDataPtr, long fromDataPtr);

}