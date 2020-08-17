package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

/**
 * An environment for running tests.
 */
public interface TestEnvironment {

  void setUpApplicationState(
      Method method,
      Configuration config, AndroidManifest appManifest);

  void tearDownApplication();

  void checkStateAfterTestFailure(Throwable t) throws Throwable;

  void resetState();
}
