package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.net.wifi.aware.DiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow implementation of {@link android.net.wifi.aware.DiscoverySession} */
@Implements(value = DiscoverySession.class, minSdk = O)
public class ShadowDiscoverySession {
  private boolean terminated = false;
  private WifiAwareManager manager;

  public static DiscoverySession newInstance() {
    return ReflectionHelpers.callConstructor(DiscoverySession.class);
  }

  @Implementation
  protected void close() {
    terminated = true;
  }

  /* Checks if a discovery session has been closed. */
  public boolean isClosed() {
    return terminated;
  }

  /* Sets a dummy WifiAwareManager for the session. */
  public void setManager(WifiAwareManager manager) {
    this.manager = manager;
  }

  /* Gets the WifiAwareManager for the session. */
  public WifiAwareManager getManager() {
    return manager;
  }
}
