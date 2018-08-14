package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.text.MeasuredParagraph;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = MeasuredParagraph.class, minSdk = P, isInAndroidSdk = false)
public class ShadowMeasuredParagraph {

  private static int nativeCounter = 0;

  @Implementation(maxSdk = P)
  protected static long nInitBuilder() {
    return ++nativeCounter;
  }
}
