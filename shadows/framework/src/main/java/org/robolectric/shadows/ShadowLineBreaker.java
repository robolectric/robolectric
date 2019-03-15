package org.robolectric.shadows;

import android.graphics.text.LineBreaker;
import android.os.Build;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = LineBreaker.class, isInAndroidSdk = false, minSdk = Build.VERSION_CODES.Q)
public class ShadowLineBreaker {

  @Implementation
  @HiddenApi
  protected static long nComputeLineBreaks(
      /* non zero */ long nativePtr,
      // Inputs
      char[] text,
      long measuredTextPtr,
      int length,
      float firstWidth,
      int firstWidthLineCount,
      float restWidth,
      int[] variableTabStops,
      int defaultTabStop,
      int indentsOffset) {
    return 1;
  }

  @Implementation
  protected static int nGetLineCount(long ptr) {
    return 1;
  }
}
