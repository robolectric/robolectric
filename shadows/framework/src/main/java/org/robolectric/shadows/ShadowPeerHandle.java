package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.net.wifi.aware.PeerHandle;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = PeerHandle.class, minSdk = O)
public class ShadowPeerHandle {

  public static PeerHandle newInstance() {
    return ReflectionHelpers.callConstructor(PeerHandle.class);
  }
}
