package org.robolectric.shadows;

import android.net.nsd.NsdManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(NsdManager.class)
public class ShadowNsdManager {

  @Implementation
  protected void init() {
    // do not blow up.
  }
}
