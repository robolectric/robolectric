package org.robolectric.internal;

import java.lang.reflect.Method;
import org.robolectric.annotation.Config;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourceTable;

public interface ParallelUniverseInterface {

  void setUpApplicationState(
      Method method,
      AndroidManifest appManifest,
      DependencyResolver jarResolver,
      Config config,
      ResourceTable compileTimeResourceTable,
      ResourceTable appResourceTable,
      ResourceTable systemResourceTable,
      FsFile compileTimeSystemResourcesFile
  );

  Thread getMainThread();

  void setMainThread(Thread newMainThread);

  void tearDownApplication();

  Object getCurrentApplication();

  void setSdkConfig(SdkConfig sdkConfig);

}
