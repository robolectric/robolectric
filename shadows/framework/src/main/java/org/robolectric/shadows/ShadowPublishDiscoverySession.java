package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.PublishDiscoverySession;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow implementation of {@link android.net.wifi.aware.PublishDiscoverySession} */
@Implements(value = PublishDiscoverySession.class, minSdk = O)
public class ShadowPublishDiscoverySession extends ShadowDiscoverySession {

  public boolean lastUpdatePassed;

  public static PublishDiscoverySession newInstance() {
    return ReflectionHelpers.callConstructor(PublishDiscoverySession.class);
  }

  @Implementation
  public void updatePublish(PublishConfig publishConfig) {
    if (isClosed() || getManager() == null) {
      lastUpdatePassed = false;
    }
    lastUpdatePassed = true;
  }
}
