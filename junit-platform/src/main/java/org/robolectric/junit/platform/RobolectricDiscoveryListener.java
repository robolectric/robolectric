package org.robolectric.junit.platform;

import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.robolectric.junit.platform.internal.ClaimRegistry;

/**
 * Scopes the classes claimed by the {@code robolectric} engine (see {@link RobolectricClaimFilter})
 * to a single discovery. Registered with {@link java.util.ServiceLoader}.
 */
public final class RobolectricDiscoveryListener implements LauncherDiscoveryListener {
  @Override
  public void launcherDiscoveryStarted(LauncherDiscoveryRequest request) {
    ClaimRegistry.reset();
  }
}
