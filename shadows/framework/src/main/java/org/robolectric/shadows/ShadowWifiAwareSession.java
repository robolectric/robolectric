package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Binder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow implementation of {@link android.net.wifi.aware.WifiAwareSession} */
@Implements(value = WifiAwareSession.class, minSdk = O)
public class ShadowWifiAwareSession {
  private boolean terminated = false;

  public static WifiAwareSession newInstance(
      WifiAwareManager manager, Binder binder, int clientId) {
    return ReflectionHelpers.callConstructor(
        WifiAwareSession.class,
        ReflectionHelpers.ClassParameter.from(WifiAwareManager.class, manager),
        ReflectionHelpers.ClassParameter.from(Binder.class, binder),
        ReflectionHelpers.ClassParameter.from(int.class, clientId));
  }

  @Implementation
  protected void close() {
    terminated = true;
  }

  /* Checks if a wifi aware session has been closed. */
  public boolean isClosed() {
    return terminated;
  }
}
