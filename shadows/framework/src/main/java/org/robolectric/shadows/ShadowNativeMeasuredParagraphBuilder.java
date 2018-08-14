// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.text.NativeMeasuredParagraph;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = NativeMeasuredParagraph.Builder.class, minSdk = Q, isInAndroidSdk = false)
public class ShadowNativeMeasuredParagraphBuilder {

  private static int nativeCounter = 0;

  @Implementation
  protected static long nInitBuilder() {
    return ++nativeCounter;
  }

  @Resetter
  public static void reset() {
    nativeCounter = 0;
  }
}
// END-INTERNAL
