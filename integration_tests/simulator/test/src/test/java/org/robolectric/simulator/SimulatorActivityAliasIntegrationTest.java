package org.robolectric.simulator;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Runs {@link SimulatorIntegrationTest} with an app whose launcher is an activity alias. */
@RunWith(JUnit4.class)
public class SimulatorActivityAliasIntegrationTest extends SimulatorIntegrationTest {
  @Override
  protected String getApkResource() {
    return "/simulator-app-alias.apk";
  }
}
