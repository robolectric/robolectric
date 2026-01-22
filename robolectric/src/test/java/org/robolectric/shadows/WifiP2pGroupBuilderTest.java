package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.wifi.p2p.WifiP2pGroup;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WifiP2pGroupBuilderTest {

  @Test
  public void build_empty() {
    WifiP2pGroup group = WifiP2pGroupBuilder.newBuilder().build();
    assertThat(group.getNetworkName()).isNull();
    assertThat(group.getInterface()).isNull();
    assertThat(group.getPassphrase()).isNull();
  }

  @Test
  public void build() {
    WifiP2pGroup group =
        WifiP2pGroupBuilder.newBuilder()
            .setNetworkName("networkName")
            .setInterface("interface")
            .setPassphrase("1234")
            .build();
    assertThat(group.getNetworkName()).isEqualTo("networkName");
    assertThat(group.getInterface()).isEqualTo("interface");
    assertThat(group.getPassphrase()).isEqualTo("1234");
  }
}
