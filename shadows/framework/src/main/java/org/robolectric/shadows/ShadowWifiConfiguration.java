package org.robolectric.shadows;

import android.net.wifi.WifiConfiguration;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(WifiConfiguration.class)
public class ShadowWifiConfiguration {
  @RealObject private WifiConfiguration realObject;

  /* Returns a copy of the {@link WifiConfiguration} it shadows. */
  public WifiConfiguration copy() {
    return new WifiConfiguration(realObject);
  }
}
