package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

public interface Bridge {

  void setUpApplicationState(
      Method method,
      Config config, AndroidManifest appManifest,
      AndroidSandbox androidSandbox);

  void tearDownApplication();

  Object getCurrentApplication();

}
