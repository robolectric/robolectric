package org.robolectric.internal;

import java.nio.file.FileSystem;
import javax.annotation.Nonnull;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

@SuppressWarnings("NewApi")
public class SdkEnvironment extends Sandbox {
  private final Sdk sdk;
  private PackageResourceTable systemResourceTable;
  private final Sdk compileTimeSdk;

  public SdkEnvironment(Sdk sdk, ClassLoader robolectricClassLoader, Sdk compileTimeSdk) {
    super(robolectricClassLoader);
    this.sdk = sdk;
    this.compileTimeSdk = compileTimeSdk;
  }

  public Sdk getCompileTimeSdk() {
    return compileTimeSdk;
  }

  public synchronized PackageResourceTable getSystemResourceTable() {
    if (systemResourceTable == null) {
      ResourcePath resourcePath = createRuntimeSdkResourcePath();
      systemResourceTable = new ResourceTableFactory().newFrameworkResourceTable(resourcePath);
    }
    return systemResourceTable;
  }

  @Nonnull
  private ResourcePath createRuntimeSdkResourcePath() {
    try {
      FileSystem zipFs = Fs.forJar(sdk.getJarPath());
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
