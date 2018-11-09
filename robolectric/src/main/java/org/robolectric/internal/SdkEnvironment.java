package org.robolectric.internal;

import javax.annotation.Nonnull;

import org.robolectric.api.Sdk;
import org.robolectric.api.SdkProvider;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

public class SdkEnvironment extends Sandbox {
  private final Sdk sdk;
  private PackageResourceTable systemResourceTable;

  public SdkEnvironment(Sdk sdk, ClassLoader robolectricClassLoader) {
    super(robolectricClassLoader);
    this.sdk = sdk;
  }

  public synchronized PackageResourceTable getSystemResourceTable(SdkProvider sdkProvider) {
    if (systemResourceTable == null) {
      ResourcePath resourcePath = createRuntimeSdkResourcePath(sdkProvider);
      systemResourceTable = new ResourceTableFactory().newFrameworkResourceTable(resourcePath);
    }
    return systemResourceTable;
  }

  @Nonnull
  private ResourcePath createRuntimeSdkResourcePath(SdkProvider sdkProvider) {
    try {
      Fs systemResFs = Fs.fromJar(sdkProvider.getPathForSdk(sdk));
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

  public Sdk getSdk() {
    return sdk;
  }
}
