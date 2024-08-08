package org.robolectric.shadows;

import android.graphics.PathIterator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link PathIterator} in LEGACY graphics. */
@Implements(
    value = PathIterator.class,
    minSdk = U.SDK_INT,
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
}
