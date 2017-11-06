package org.robolectric.internal;

import javax.annotation.Nonnull;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

public class SdkEnvironment extends Sandbox {
  private final SdkConfig sdkConfig;
  private FsFile compileTimeSystemResourcesFile;
  private PackageResourceTable systemResourceTable;

  public SdkEnvironment(SdkConfig sdkConfig, ClassLoader robolectricClassLoader) {
    super(robolectricClassLoader);
    this.sdkConfig = sdkConfig;
  }

  public synchronized FsFile getCompileTimeSystemResourcesFile(DependencyResolver dependencyResolver) {
    if (compileTimeSystemResourcesFile == null) {
      DependencyJar compileTimeJar = new SdkConfig(27).getAndroidSdkDependency();
      compileTimeSystemResourcesFile =
          Fs.newFile(dependencyResolver.getLocalArtifactUrl(compileTimeJar).getFile());
    }
    return compileTimeSystemResourcesFile;
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
      // TODO: verify these can be loaded via raw-res path
      return new ResourcePath(androidRClass,
          systemResFs.join("raw-res/res"),
          systemResFs.join("raw-res/assets"),
          androidInternalRClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public SdkConfig getSdkConfig() {
    return sdkConfig;
  }
}
