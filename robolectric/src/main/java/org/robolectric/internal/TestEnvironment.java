package org.robolectric.internal;

import org.robolectric.pluginapi.config.ConfiguredTest;

/**
 * An environment for running tests.
 */
public interface TestEnvironment {

  void before(ConfiguredTest configuredTest);

  void after(ConfiguredTest configuredTest);

  void checkStateAfterTestFailure(Throwable t) throws Throwable;

  void resetState();
}
