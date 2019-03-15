package org.robolectric.shadows;

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

  @Implementation
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
