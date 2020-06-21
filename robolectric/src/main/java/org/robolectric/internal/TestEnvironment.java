package org.robolectric.internal;

import org.robolectric.pluginapi.config.Configuration;

/**
 * An environment for running tests.
 */
public interface TestEnvironment {

  void setUpApplicationState(Configuration configuration, String testName);

  void tearDownApplication();

  void checkStateAfterTestFailure(Throwable t) throws Throwable;

  void resetState();
}
