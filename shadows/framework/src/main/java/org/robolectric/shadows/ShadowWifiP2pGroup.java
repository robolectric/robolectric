package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.wifi.p2p.WifiP2pGroup;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(WifiP2pGroup.class)
public class ShadowWifiP2pGroup {

  @RealObject private WifiP2pGroup realObject;

  public void setInterface(String intf) {
    reflector(WifiP2pGroupReflector.class, realObject).setInterface(intf);
  }

  public void setPassphrase(String passphrase) {
    reflector(WifiP2pGroupReflector.class, realObject).setInterface(passphrase);
  }

  public void setNetworkName(String networkName) {
    reflector(WifiP2pGroupReflector.class, realObject).setInterface(networkName);
  }

  @ForType(WifiP2pGroup.class)
  interface WifiP2pGroupReflector {

    @Direct
    void setInterface(String intf);

    @Direct
    void setPassphrase(String passphrase);

    @Direct
    void setNetworkName(String networkName);
  }
}
