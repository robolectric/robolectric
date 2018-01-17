// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "android.text.MeasuredParagraph", minSdk = P, isInAndroidSdk = false)
public class ShadowMeasuredParagraph {

  private static int nativeCounter = 0;

  @Implementation
  public static long nInitBuilder() {
    return ++nativeCounter;
  }


  @Implementation
  public static void nAddStyleRun(long nativeBuilderPtr, long paintPtr, int start, int end,
      boolean isRtl) {
  }

  @Implementation
  public static void nAddReplacementRun(long nativeBuilderPtr, long paintPtr, int start, int end,
      float width) {
  }

  @Implementation
  public static long nBuildNativeMeasuredParagraph(long nativeBuilderPtr, char[] text) {
    return 1;
  }

  @Implementation
  public static void nFreeBuilder(long nativeBuilderPtr) {
  }

  @Implementation
  public static long nGetReleaseFunc() {
    return 1;
  }

}

// END-INTERNAL