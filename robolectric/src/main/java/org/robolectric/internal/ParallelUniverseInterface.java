package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceTable;

public interface ParallelUniverseInterface {
  void resetStaticState(Config config);

  void setUpApplicationState(Method method, TestLifecycle testLifecycle, AndroidManifest appManifest, Config config,
                             ResourceTable compiletimeResourceTable, ResourceTable appResourceTable,
                             ResourceTable systemResourceTable);

  Thread getMainThread();

  void setMainThread(Thread newMainThread);

  void tearDownApplication();

  Object getCurrentApplication();

  void setSdkConfig(SdkConfig sdkConfig);

}
