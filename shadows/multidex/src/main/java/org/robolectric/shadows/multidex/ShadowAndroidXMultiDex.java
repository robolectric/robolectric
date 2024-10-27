package org.robolectric.shadows.multidex;

import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * @deprecated With Robolectric's min SDK version being 21, using Multidex libraries is no longer
 *     necessary. This class will be removed in Robolectric 4.15.
 */
@Deprecated
@Implements(className = "androidx.multidex.MultiDex")
@SuppressWarnings("robolectric.internal.IgnoreMissingClass")
public class ShadowAndroidXMultiDex {

  @Implementation
  protected static void install(Context context) {
    // Do nothing since with Robolectric nothing is dexed.
  }
}
