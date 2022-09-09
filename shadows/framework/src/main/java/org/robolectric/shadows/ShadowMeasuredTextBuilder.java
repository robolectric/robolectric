package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S_V2;

import android.graphics.text.MeasuredText;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(
    value = MeasuredText.Builder.class,
    minSdk = Build.VERSION_CODES.Q,
    isInAndroidSdk = false)
public class ShadowMeasuredTextBuilder {

  private static int nativeCounter = 0;

  @Implementation
  protected static long nInitBuilder() {
    return ++nativeCounter;
  }

  @Implementation(maxSdk = S_V2)
  protected static long nBuildMeasuredText(
      long nativeBuilderPtr,
      long hintMtPtr,
      char[] text,
      boolean computeHyphenation,
      boolean computeLayout) {
    return ++nativeCounter;
  }

  @Resetter
  public static void reset() {
    nativeCounter = 0;
  }
}
