package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;

public interface Environment {

  void setUpApplicationState(
      Method method,
      Configuration config, AndroidManifest appManifest);

  void tearDownApplication();

  void resetState();
}
