package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.AndroidManifest;
import org.robolectric.SdkConfig;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.res.ResourceLoader;

public interface ParallelUniverseInterface {
  public void resetStaticState();

  void setUpApplicationState(Method method, TestLifecycle testLifecycle, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config);

  void tearDownApplication();

  Object getCurrentApplication();

  void setSdkConfig(SdkConfig sdkConfig);
}
