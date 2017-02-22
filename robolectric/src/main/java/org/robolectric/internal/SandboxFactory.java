package org.robolectric.internal;

import org.jetbrains.annotations.NotNull;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.util.Pair;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.Map;

public class SandboxFactory {
  public static final SandboxFactory INSTANCE = new SandboxFactory();

  /** The factor for cache size. See {@link #CACHE_SIZE} for details. */
  private static final int CACHE_SIZE_FACTOR = 3;

  /** We need to set the cache size of class loaders more than the number of supported APIs as different tests may have different configurations. */
  private static final int CACHE_SIZE = SdkConfig.getSupportedApis().size() * CACHE_SIZE_FACTOR;

  // Simple LRU Cache. SdkEnvironments are unique across InstrumentationConfiguration and SdkConfig
  private final LinkedHashMap<Pair<InstrumentationConfiguration, SdkConfig>, SdkEnvironment> sdkToEnvironment = new LinkedHashMap<Pair<InstrumentationConfiguration, SdkConfig>, SdkEnvironment>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Pair<InstrumentationConfiguration, SdkConfig>, SdkEnvironment> eldest) {
      return size() > CACHE_SIZE;
    }
  };

  public synchronized SdkEnvironment getSdkEnvironment(InstrumentationConfiguration instrumentationConfig, DependencyResolver dependencyResolver, SdkConfig sdkConfig) {
    Pair<InstrumentationConfiguration, SdkConfig> key = Pair.create(instrumentationConfig, sdkConfig);

    SdkEnvironment sdkEnvironment = sdkToEnvironment.get(key);
    if (sdkEnvironment == null) {
      URL url = dependencyResolver.getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency());

      ClassLoader robolectricClassLoader = createClassLoader(instrumentationConfig, url);
      sdkEnvironment = new SdkEnvironment(sdkConfig, robolectricClassLoader);

      sdkToEnvironment.put(key, sdkEnvironment);
    }
    return sdkEnvironment;
  }

  @NotNull
  public ClassLoader createClassLoader(InstrumentationConfiguration instrumentationConfig, URL... urls) {
    URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    return new SandboxClassLoader(systemClassLoader, instrumentationConfig, urls);
  }

}
