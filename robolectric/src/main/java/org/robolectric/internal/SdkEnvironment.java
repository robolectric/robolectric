package org.robolectric.internal;

import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;
import org.robolectric.util.Util;

@SuppressWarnings("NewApi")
public class SdkEnvironment extends Sandbox {
  private final Sdk sdk;
  private Path compileTimeSystemResourcesFile;
  private PackageResourceTable systemResourceTable;
  private final Sdk compileTimeSdk;

  public SdkEnvironment(Sdk sdk, ClassLoader robolectricClassLoader,
      Sdk compileTimeSdk) {
    super(robolectricClassLoader);
    this.sdk = sdk;
    this.compileTimeSdk = compileTimeSdk;
  }

  public synchronized Path getCompileTimeSystemResourcesFile(
      DependencyResolver dependencyResolver) {
    if (compileTimeSystemResourcesFile == null) {
      DependencyJar compileTimeJar = compileTimeSdk.getAndroidSdkDependency();
      URL localArtifactUrl = dependencyResolver.getLocalArtifactUrl(compileTimeJar);
      compileTimeSystemResourcesFile = Util.pathFrom(localArtifactUrl);
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
      URL sdkUrl = dependencyResolver.getLocalArtifactUrl(sdk.getAndroidSdkDependency());
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

  public Sdk getSdk() {
    return sdk;
  }
}
