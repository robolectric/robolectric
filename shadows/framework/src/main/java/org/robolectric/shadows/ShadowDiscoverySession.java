package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.net.wifi.aware.DiscoverySession;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = DiscoverySession.class, minSdk = O)
public class ShadowDiscoverySession {

  public static DiscoverySession newInstance() {
    return ReflectionHelpers.callConstructor(DiscoverySession.class);
  }
}
