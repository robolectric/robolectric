// BEGIN-INTERNAL
package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "android.text.PrecomputedText", minSdk = P, isInAndroidSdk = false)
public class ShadowPrecomputedText {

  private static int nativeCounter = 0;

  @Implementation
  public static long nInitBuilder() {
    return ++nativeCounter;
  }
}

// END-INTERNAL
