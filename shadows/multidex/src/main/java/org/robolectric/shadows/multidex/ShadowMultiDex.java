package org.robolectric.shadows.multidex;

import android.content.Context;
import androidx.multidex.MultiDex;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** No-op shadow for {@link androidx.multidex.MultiDex} */
@Implements(MultiDex.class)
public class ShadowMultiDex {

  @Implementation
  protected static void install(Context context) {
    // Do nothing since with Robolectric nothing is dexed.
  }
}
