package org.robolectric.shadows;

import android.content.Intent;
import android.net.PlatformVpnProfile;
import android.net.VpnManager;
import android.net.VpnProfileState;
import android.os.Build.VERSION_CODES;
import java.util.UUID;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link VpnManager}. */
@Implements(value = VpnManager.class, minSdk = VERSION_CODES.R, isInAndroidSdk = false)
public class ShadowVpnManager {

  private VpnProfileState vpnProfileState;
  private Intent provisionVpnProfileIntent;

  @Implementation
  protected void deleteProvisionedVpnProfile() {
    vpnProfileState = null;
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected VpnProfileState getProvisionedVpnProfileState() {
    return vpnProfileState;
  }

  /**
   * @see #setProvisionVpnProfileResult(Intent).
   */
  @Implementation
  protected Intent provisionVpnProfile(PlatformVpnProfile profile) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.TIRAMISU) {
      vpnProfileState = new VpnProfileState(VpnProfileState.STATE_DISCONNECTED, null, false, false);
    }
    return provisionVpnProfileIntent;
  }

  /** Sets the return value of #provisionVpnProfile(PlatformVpnProfile). */
  public void setProvisionVpnProfileResult(Intent intent) {
    provisionVpnProfileIntent = intent;
  }

  @Implementation
  protected void startProvisionedVpnProfile() {
    startProvisionedVpnProfileSession();
  }

  @Implementation(minSdk = VERSION_CODES.TIRAMISU)
  protected String startProvisionedVpnProfileSession() {
    String sessionKey = UUID.randomUUID().toString();
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.TIRAMISU) {
      vpnProfileState =
          new VpnProfileState(VpnProfileState.STATE_CONNECTED, sessionKey, false, false);
    }
    return sessionKey;
  }

  @Implementation
  protected void stopProvisionedVpnProfile() {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.TIRAMISU) {
      vpnProfileState = new VpnProfileState(VpnProfileState.STATE_DISCONNECTED, null, false, false);
    }
  }
}
