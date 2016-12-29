package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.res.ResourceProvider;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.RoutingResourceProvider;

public interface ParallelUniverseInterface {
  void resetStaticState(Config config);

  void setUpApplicationState(Method method, TestLifecycle testLifecycle, AndroidManifest appManifest, Config config,
                             ResourceProvider compiletimeResourceProvider, ResourceProvider appResourceProvider,
                             ResourceProvider systemResourceProvider);

  Thread getMainThread();

  void setMainThread(Thread newMainThread);

  void tearDownApplication();

  Object getCurrentApplication();

  void setSdkConfig(SdkConfig sdkConfig);

}
