package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.net.wifi.p2p.WifiP2pGroup;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(WifiP2pGroup.class)
public class ShadowWifiP2pGroup {

  @RealObject
  private WifiP2pGroup realObject;

  public void setInterface(String intf) {
    directlyOn(realObject, WifiP2pGroup.class).setInterface(intf);
  }

  public void setPassphrase(String passphrase) {
    directlyOn(realObject, WifiP2pGroup.class).setInterface(passphrase);
  }

  public void setNetworkName(String networkName) {
    directlyOn(realObject, WifiP2pGroup.class).setInterface(networkName);
  }
}
