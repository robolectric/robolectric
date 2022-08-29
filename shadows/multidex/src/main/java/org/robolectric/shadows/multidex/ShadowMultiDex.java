package org.robolectric.shadows.multidex;

import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** No-op shadow for {@link android.support.multidex.MultiDex}. */
@Implements(className = "android.support.multidex.MultiDex")
@SuppressWarnings("robolectric.internal.IgnoreMissingClass")
public class ShadowMultiDex {

  @Implementation
  protected static void install(Context context) {
    // Do nothing since with Robolectric nothing is dexed.
  }
}
