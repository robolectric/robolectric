package org.robolectric.internal;

import javax.annotation.Nonnull;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

public class SdkEnvironment extends Sandbox {
  private final SdkConfig sdkConfig;
  private PackageResourceTable systemResourceTable;

  public SdkEnvironment(SdkConfig sdkConfig, ClassLoader robolectricClassLoader) {
    super(robolectricClassLoader);
    this.sdkConfig = sdkConfig;
  }

  public synchronized PackageResourceTable getSystemResourceTable(DependencyResolver dependencyResolver) {
    if (systemResourceTable == null) {
      ResourcePath resourcePath = createRuntimeSdkResourcePath(dependencyResolver);
      systemResourceTable = new ResourceTableFactory().newFrameworkResourceTable(resourcePath);
    }
    return systemResourceTable;
  }

  @Nonnull
  private ResourcePath createRuntimeSdkResourcePath(DependencyResolver dependencyResolver) {
    try {
      Fs systemResFs = Fs.fromJar(dependencyResolver.getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency()));
      Class<?> androidRClass = getRobolectricClassLoader().loadClass("android.R");
      Class<?> androidInternalRClass = getRobolectricClassLoader().loadClass("com.android.internal.R");
      return new ResourcePath(androidRClass,
          systemResFs.join("res"), systemResFs.join("assets"),
          androidInternalRClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public SdkConfig getSdkConfig() {
    return sdkConfig;
  }
}
