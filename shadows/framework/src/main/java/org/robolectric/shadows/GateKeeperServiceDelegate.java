package org.robolectric.shadows;

import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;

/**
 * Serves {@code IGateKeeperService}, whose secure user id tells keystore which secure lock screen a
 * key requiring user authentication is bound to. It comes from {@link ShadowKeyguardManager}.
 */
class GateKeeperServiceDelegate extends IGateKeeperService.Stub {
  @Override
  public long getSecureUserId(int userId) {
    return ShadowKeyguardManager.getSecureUserId(userId);
  }

  @Override
  public GateKeeperResponse enroll(
      int userId, byte[] currentPasswordHandle, byte[] currentPassword, byte[] desiredPassword) {
    throw KeystoreServiceDelegate.unsupported("enroll");
  }

  @Override
  public GateKeeperResponse verify(int userId, byte[] enrolledPasswordHandle, byte[] password) {
    throw KeystoreServiceDelegate.unsupported("verify");
  }

  @Override
  public GateKeeperResponse verifyChallenge(
      int userId, long challenge, byte[] enrolledPasswordHandle, byte[] password) {
    throw KeystoreServiceDelegate.unsupported("verifyChallenge");
  }

  @Override
  public void clearSecureUserId(int userId) {
    throw KeystoreServiceDelegate.unsupported("clearSecureUserId");
  }

  @Override
  public void reportDeviceSetupComplete() {
    throw KeystoreServiceDelegate.unsupported("reportDeviceSetupComplete");
  }
}
