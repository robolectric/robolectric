package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow implementation of {@link android.net.wifi.aware.SubscribeDiscoverySession} */
@Implements(value = SubscribeDiscoverySession.class, minSdk = O)
public class ShadowSubscribeDiscoverySession extends ShadowDiscoverySession {

  public boolean lastUpdatePassed;

  public static SubscribeDiscoverySession newInstance() {
    return ReflectionHelpers.callConstructor(SubscribeDiscoverySession.class);
  }

  @Implementation
  public void updateSubscribe(SubscribeConfig subscribeConfig) {
    if (isClosed() || getManager() == null) {
      lastUpdatePassed = false;
    }
    lastUpdatePassed = true;
  }
}
