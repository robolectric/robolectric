// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.text.NativeLineBreaker;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(
    value = NativeLineBreaker.class,
    isInAndroidSdk = false,
    minSdk = Q,
    looseSignatures = true)
public class ShadowNativeLineBreaker {

  @Implementation
  @HiddenApi
  protected static int nComputeLineBreaks(
      Object nativePtr,
      Object text,
      Object measuredTextPtr,
      Object length,
      Object firstWidth,
      Object firstWidthLineCount,
      Object restWidth,
      Object variableTabStops,
      Object defaultTabStop,
      Object indentsOffset,
      Object recycle,
      Object recycleLength,
      Object recycleBreaks,
      Object recycleWidths,
      Object recycleAscents,
      Object recycleDescents,
      Object recycleFlags) {
    return 1;
  }
}
// END-INTERNAL
