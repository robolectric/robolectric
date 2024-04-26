package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;
import android.net.Ikev2VpnProfile;
import android.net.VpnManager;
import android.net.VpnProfileState;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.R)
public class ShadowVpnManagerTest {
  private VpnManager vpnManager;
  private ShadowVpnManager shadowVpnManager;

  @Before
  public void setUp() throws Exception {
    vpnManager = ApplicationProvider.getApplicationContext().getSystemService(VpnManager.class);
    shadowVpnManager = Shadow.extract(vpnManager);
  }

  @Test
  public void provisionVpnProfile() {
    Intent intent = new Intent("foo");
    shadowVpnManager.setProvisionVpnProfileResult(intent);

    assertThat(
            vpnManager.provisionVpnProfile(
                new Ikev2VpnProfile.Builder("server", "local.identity")
                    .setAuthPsk(new byte[0])
                    .build()))
        .isSameInstanceAs(intent);

    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.TIRAMISU) {
      VpnProfileState state = vpnManager.getProvisionedVpnProfileState();
      assertThat(state.getState()).isEqualTo(VpnProfileState.STATE_DISCONNECTED);
      assertThat(state.getSessionId()).isNull();
    }
  }

  @Test
  public void deleteVpnProfile() {
    vpnManager.provisionVpnProfile(
        new Ikev2VpnProfile.Builder("server", "local.identity").setAuthPsk(new byte[0]).build());
    vpnManager.deleteProvisionedVpnProfile();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void deleteVpnProfile_tiramisu() {
    vpnManager.provisionVpnProfile(
        new Ikev2VpnProfile.Builder("server", "local.identity").setAuthPsk(new byte[0]).build());
    assertThat(vpnManager.getProvisionedVpnProfileState()).isNotNull();

    vpnManager.deleteProvisionedVpnProfile();
    assertThat(vpnManager.getProvisionedVpnProfileState()).isNull();
  }

  @Test
  public void startAndStopVpnProfile() {
    vpnManager.provisionVpnProfile(
        new Ikev2VpnProfile.Builder("server", "local.identity").setAuthPsk(new byte[0]).build());
    vpnManager.startProvisionedVpnProfile();
    vpnManager.stopProvisionedVpnProfile();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void startAndStopVpnProfile_tiramisu() {
    vpnManager.provisionVpnProfile(
        new Ikev2VpnProfile.Builder("server", "local.identity").setAuthPsk(new byte[0]).build());
    String sessionKey = vpnManager.startProvisionedVpnProfileSession();
    VpnProfileState state = vpnManager.getProvisionedVpnProfileState();
    assertThat(state.getState()).isEqualTo(VpnProfileState.STATE_CONNECTED);
    assertThat(state.getSessionId()).isEqualTo(sessionKey);
    assertThat(state.isAlwaysOn()).isFalse();
    assertThat(state.isLockdownEnabled()).isFalse();

    vpnManager.stopProvisionedVpnProfile();
    state = vpnManager.getProvisionedVpnProfileState();
    assertThat(state.getState()).isEqualTo(VpnProfileState.STATE_DISCONNECTED);
    assertThat(state.getSessionId()).isNull();
    assertThat(state.isAlwaysOn()).isFalse();
    assertThat(state.isLockdownEnabled()).isFalse();
  }
}
