package org.robolectric.internal;

import org.robolectric.internal.bytecode.InstrumentingClassLoader;
import org.robolectric.internal.bytecode.InstrumentingClassLoaderConfig;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.util.Pair;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnvHolder {

  // Typical test suites will use a single test runner, therefore have a maximum of one SdkEnvironment per API level.
  private static final int CACHE_SIZE = SdkConfig.getSupportedApis().size();

  // Simple LRU Cache. SdkEnvironments are unique across InstrumentingClassloaderConfig and SdkConfig
  private static final LinkedHashMap<Pair<InstrumentingClassLoaderConfig, SdkConfig>, SdkEnvironment> sdkToEnvironment = new LinkedHashMap<Pair<InstrumentingClassLoaderConfig, SdkConfig>, SdkEnvironment>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Pair<InstrumentingClassLoaderConfig, SdkConfig>, SdkEnvironment> eldest) {
      return size() > CACHE_SIZE;
    }
  };

  private final InstrumentingClassLoaderConfig instrumentationConfig;
  private final DependencyResolver dependencyResolver;

  public EnvHolder(InstrumentingClassLoaderConfig instrumentationConfig, DependencyResolver dependencyResolver) {
    this.instrumentationConfig = instrumentationConfig;
    this.dependencyResolver = dependencyResolver;
  }

  public synchronized SdkEnvironment getSdkEnvironment(SdkConfig sdkConfig) {

    Pair<InstrumentingClassLoaderConfig, SdkConfig> key = Pair.create(instrumentationConfig, sdkConfig);

    SdkEnvironment sdkEnvironment = sdkToEnvironment.get(key);
    if (sdkEnvironment == null) {
      URL[] urls = dependencyResolver.getLocalArtifactUrls(sdkConfig.getSdkClasspathDependencies());
      ClassLoader robolectricClassLoader = new InstrumentingClassLoader(instrumentationConfig, urls);
      sdkEnvironment = new SdkEnvironment(sdkConfig, robolectricClassLoader);
      sdkToEnvironment.put(key, sdkEnvironment);
    }
    return sdkEnvironment;
  }
}
