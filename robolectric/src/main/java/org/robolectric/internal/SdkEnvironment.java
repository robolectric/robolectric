package org.robolectric.internal;

import com.google.auto.service.AutoService;
import java.nio.file.FileSystem;
import javax.annotation.Nonnull;
import javax.inject.Named;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceTable;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTableFactory;

@SuppressWarnings("NewApi")
@AutoService(Sandbox.class)
public class SdkEnvironment extends Sandbox {
  private final Sdk sdk;
  private PackageResourceTable systemResourceTable;
  private final Sdk compileTimeSdk;

  public SdkEnvironment(
      @Named("runtime") Sdk sdk,
      ClassLoader robolectricClassLoader,
      @Named("compileTime") Sdk compileTimeSdk,
      Interceptors interceptors) {
    super(robolectricClassLoader, interceptors);
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

  @Override
  public void configure(ClassHandler classHandler) {
    ((ShadowWrangler) classHandler).setApiLevel(sdk.getApiLevel());
    super.configure(classHandler);
  }
}
