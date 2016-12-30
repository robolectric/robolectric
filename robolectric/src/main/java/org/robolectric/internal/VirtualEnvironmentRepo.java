package org.robolectric.internal;

import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentingClassLoader;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.util.Pair;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class VirtualEnvironmentRepo {

  /** The factor for cache size. See {@link #CACHE_SIZE} for details. */
  private static final int CACHE_SIZE_FACTOR = 3;

  /** We need to set the cache size of class loaders more than the number of supported APIs as different tests may have different configurations. */
  private static final int CACHE_SIZE = SdkConfig.getSupportedApis().size() * CACHE_SIZE_FACTOR;

  // Simple LRU Cache. SdkEnvironments are unique across InstrumentingClassloaderConfig and SdkConfig
  private static final LinkedHashMap<Pair<InstrumentationConfiguration, SdkConfig>, VirtualEnvironment> sdkToEnvironment = new LinkedHashMap<Pair<InstrumentationConfiguration, SdkConfig>, VirtualEnvironment>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Pair<InstrumentationConfiguration, SdkConfig>, VirtualEnvironment> eldest) {
      return size() > CACHE_SIZE;
    }
  };

  private final InstrumentationConfiguration instrumentationConfig;
  private final DependencyResolver dependencyResolver;

  public VirtualEnvironmentRepo(InstrumentationConfiguration instrumentationConfig, DependencyResolver dependencyResolver) {
    this.instrumentationConfig = instrumentationConfig;
    this.dependencyResolver = dependencyResolver;
  }

  public synchronized VirtualEnvironment getVirtualEnvironment(SdkConfig sdkConfig) {

    Pair<InstrumentationConfiguration, SdkConfig> key = Pair.create(instrumentationConfig, sdkConfig);

    VirtualEnvironment virtualEnvironment = sdkToEnvironment.get(key);
    if (virtualEnvironment == null) {
      URL[] urls = dependencyResolver.getLocalArtifactUrls(
          sdkConfig.getAndroidSdkDependency(),
          sdkConfig.getCoreShadowsDependency());

      ClassLoader robolectricClassLoader = new InstrumentingClassLoader(instrumentationConfig, urls);
      virtualEnvironment = new VirtualEnvironment(sdkConfig, robolectricClassLoader);
      sdkToEnvironment.put(key, virtualEnvironment);
    }
    return virtualEnvironment;
  }
}
