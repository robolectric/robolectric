package org.robolectric.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Runs {@link SimulatorIntegrationTest} with an app that also has a launcher from a library, which
 * sorts before the app's own launcher by name.
 */
@RunWith(JUnit4.class)
public class SimulatorLibraryLauncherIntegrationTest extends SimulatorIntegrationTest {
  @Override
  protected String getApkResource() {
    return "/simulator-app-library.apk";
  }
}
