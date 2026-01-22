package org.robolectric.shadows;

import android.net.wifi.p2p.WifiP2pGroup;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** Builder for {@link WifiP2pGroup}. */
public class WifiP2pGroupBuilder {

  private String intf;
  private String passphrase;
  private String networkName;

  private WifiP2pGroupBuilder() {}

  public static WifiP2pGroupBuilder newBuilder() {
    return new WifiP2pGroupBuilder();
  }

  @CanIgnoreReturnValue
  public WifiP2pGroupBuilder setInterface(String intf) {
    this.intf = intf;
    return this;
  }

  @CanIgnoreReturnValue
  public WifiP2pGroupBuilder setPassphrase(String passphrase) {
    this.passphrase = passphrase;
    return this;
  }

  @CanIgnoreReturnValue
  public WifiP2pGroupBuilder setNetworkName(String networkName) {
    this.networkName = networkName;
    return this;
  }

  public WifiP2pGroup build() {
    WifiP2pGroup group = new WifiP2pGroup();
    group.setInterface(intf);
    group.setPassphrase(passphrase);
    group.setNetworkName(networkName);
    return group;
  }
}
