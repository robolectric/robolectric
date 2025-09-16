package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import android.graphics.PathIterator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link PathIterator} in LEGACY graphics. */
@Implements(
    value = PathIterator.class,
    minSdk = UPSIDE_DOWN_CAKE,
    isInAndroidSdk = false /* disable shadowOf generation */)
public class ShadowPathIterator {

  /**
   * By default, Robolectric instrumentation of this native method will cause it to return zero,
   * which conveys {@link PathIterator#VERB_MOVE}. To avoid infinite loops, update it to return
   * {@link PathIterator#VERB_DONE}.
   */
  @Implementation
  protected static int nNext(long nativeIterator, long pointsAddress) {
    return PathIterator.VERB_DONE;
  }

  /** Also shadow the upcoming indevelopment nNextHost */
  @Implementation(minSdk = VANILLA_ICE_CREAM)
  protected static int nNextHost(long nativeIterator, float[] points) {
    return PathIterator.VERB_DONE;
  }
}
