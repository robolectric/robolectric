package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S_V2;

import android.net.nsd.NsdManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(NsdManager.class)
public class ShadowNsdManager {

  @Implementation(maxSdk = S_V2)
  protected void init() {
    // do not blow up.
  }
}
