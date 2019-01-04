package org.robolectric.internal;

import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

@SuppressWarnings("NewApi")
public class SdkEnvironment extends Sandbox {
  private final SdkConfig sdkConfig;
  private Path compileTimeSystemResourcesFile;
  private PackageResourceTable systemResourceTable;
  private final SdkConfig compileTimeSdkConfig;

  public SdkEnvironment(SdkConfig sdkConfig, ClassLoader robolectricClassLoader,
      SdkConfig compileTimeSdkConfig) {
    super(robolectricClassLoader);
    this.sdkConfig = sdkConfig;
    this.compileTimeSdkConfig = compileTimeSdkConfig;
  }

  public synchronized Path getCompileTimeSystemResourcesFile(
      DependencyResolver dependencyResolver) {
    if (compileTimeSystemResourcesFile == null) {
      DependencyJar compileTimeJar = compileTimeSdkConfig.getAndroidSdkDependency();
      compileTimeSystemResourcesFile =
          Paths.get(dependencyResolver.getLocalArtifactUrl(compileTimeJar).getFile());
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
      URL sdkUrl = dependencyResolver.getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency());
      FileSystem zipFs = Fs.forJar(sdkUrl);
      Class<?> androidRClass = getRobolectricClassLoader().loadClass("android.R");
      Class<?> androidInternalRClass = getRobolectricClassLoader().loadClass("com.android.internal.R");
      // TODO: verify these can be loaded via raw-res path
      return new ResourcePath(
          androidRClass,
          zipFs.getPath("raw-res/res"),
          zipFs.getPath("raw-res/assets"),
          androidInternalRClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public SdkConfig getSdkConfig() {
    return sdkConfig;
  }
}
