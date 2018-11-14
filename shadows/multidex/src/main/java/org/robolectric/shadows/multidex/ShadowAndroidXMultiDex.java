package org.robolectric.shadows.multidex;

import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "androidx.multidex.MultiDex")
@SuppressWarnings("robolectric.internal.IgnoreMissingClass")
public class ShadowAndroidXMultiDex {

  @Implementation
  protected static void install(Context context) {
    // Do nothing since with Robolectric nothing is dexed.
  }

}
