package org.robolectric.internal;

import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.res.Fs;
import org.robolectric.res.PackageResourceLoader;
import org.robolectric.res.ResourceExtractor;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;

import java.util.HashMap;
import java.util.Map;

public class SdkEnvironment {
  private final SdkConfig sdkConfig;
  private final ClassLoader robolectricClassLoader;
  public final Map<ShadowMap, ShadowWrangler> classHandlersByShadowMap = new HashMap<>();
  private ResourceLoader systemResourceLoader;

  public SdkEnvironment(SdkConfig sdkConfig, ClassLoader robolectricClassLoader) {
    this.sdkConfig = sdkConfig;
    this.robolectricClassLoader = robolectricClassLoader;
  }

  public PackageResourceLoader createSystemResourceLoader(DependencyResolver dependencyResolver) {
    Fs systemResFs = Fs.fromJar(dependencyResolver.getLocalArtifactUrl(sdkConfig.getSystemResourceDependency()));
    ResourceExtractor resourceExtractor;
    try {
      resourceExtractor = new ResourceExtractor(getRobolectricClassLoader().loadClass("com.android.internal.R"), getRobolectricClassLoader().loadClass("android.R"));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    ResourcePath resourcePath = new ResourcePath(resourceExtractor.getProcessedRFile(), resourceExtractor.getPackageName(), systemResFs.join("res"), systemResFs.join("assets"));
    return new PackageResourceLoader(resourcePath, resourceExtractor);
  }

  public synchronized ResourceLoader getSystemResourceLoader(DependencyResolver dependencyResolver) {
    if (systemResourceLoader == null) {
      systemResourceLoader = createSystemResourceLoader(dependencyResolver);
    }
    return systemResourceLoader;
  }

  public Class<?> bootstrappedClass(Class<?> testClass) {
    try {
      return robolectricClassLoader.loadClass(testClass.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public ClassLoader getRobolectricClassLoader() {
    return robolectricClassLoader;
  }

  public SdkConfig getSdkConfig() {
    return sdkConfig;
  }
}
