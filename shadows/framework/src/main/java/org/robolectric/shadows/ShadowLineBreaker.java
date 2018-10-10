// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.graphics.text.LineBreaker;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(
    value = LineBreaker.class,
    isInAndroidSdk = false,
    minSdk = Q,
    looseSignatures = true)
public class ShadowLineBreaker {

  @Implementation
  @HiddenApi
  protected static long nComputeLineBreaks(
      Object nativePtr,
      Object text,
      Object measuredTextPtr,
      Object length,
      Object firstWidth,
      Object firstWidthLineCount,
      Object restWidth,
      Object variableTabStops,
      Object defaultTabStop,
      Object indentsOffset) {
    return 1;
  }

  @Implementation
  protected static int nGetLineCount(long ptr) {
    return 1;
  }
}
// END-INTERNAL
