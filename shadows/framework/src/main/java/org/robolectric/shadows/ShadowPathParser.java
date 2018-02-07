package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.util.PathParser;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = PathParser.class, minSdk = N, isInAndroidSdk = false)
public class ShadowPathParser {

  @Implementation
  public static long nCreatePathDataFromString(String pathString, int stringLength) {
    return 1;
  }

  @Implementation
  public static boolean nInterpolatePathData(long outDataPtr, long fromDataPtr,
                                             long toDataPtr, float fraction) {
    return true;
  }

  @Implementation
  public static boolean nCanMorph(long fromDataPtr, long toDataPtr) {
    return true;
  }
}