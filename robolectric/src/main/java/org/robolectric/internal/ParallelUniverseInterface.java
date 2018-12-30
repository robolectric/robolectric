package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.ApkLoader;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

public interface ParallelUniverseInterface {

  void setUpApplicationState(
      ApkLoader apkLoader, Method method,
      Config config, AndroidManifest appManifest,
      SdkEnvironment sdkEnvironment);

  void tearDownApplication();

  Object getCurrentApplication();

}
