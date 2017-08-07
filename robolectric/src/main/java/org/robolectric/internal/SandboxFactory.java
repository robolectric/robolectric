package org.robolectric.internal;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.dependency.DependencyResolver;

public class SandboxFactory {
  public static final SandboxFactory INSTANCE = new SandboxFactory();

  /** The factor for cache size. See {@link #CACHE_SIZE} for details. */
  private static final int CACHE_SIZE_FACTOR = 3;

  /** We need to set the cache size of class loaders more than the number of supported APIs as different tests may have different configurations. */
  private static final int CACHE_SIZE = SdkConfig.getSupportedApis().size() * CACHE_SIZE_FACTOR;

  // Simple LRU Cache. SdkEnvironments are unique across InstrumentationConfiguration, SdkConfig, and enableRendering
  private final LinkedHashMap<EnvKey, SdkEnvironment> sdkToEnvironment = new LinkedHashMap<EnvKey, SdkEnvironment>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<EnvKey, SdkEnvironment> eldest) {
      return size() > CACHE_SIZE;
    }
  };

  public synchronized SdkEnvironment getSdkEnvironment(
      InstrumentationConfiguration instrumentationConfig, DependencyResolver dependencyResolver,
      SdkConfig sdkConfig, boolean enableRendering) {
    EnvKey key = new EnvKey(instrumentationConfig, sdkConfig, enableRendering);

    SdkEnvironment sdkEnvironment = sdkToEnvironment.get(key);
    if (sdkEnvironment == null) {
      URL url = dependencyResolver.getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency());

      ClassLoader robolectricClassLoader = createClassLoader(instrumentationConfig, url);
      sdkEnvironment = new SdkEnvironment(sdkConfig, robolectricClassLoader);

      sdkToEnvironment.put(key, sdkEnvironment);
    }
    return sdkEnvironment;
  }

  @Nonnull
  public ClassLoader createClassLoader(InstrumentationConfiguration instrumentationConfig, URL... urls) {
    URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    return new SandboxClassLoader(systemClassLoader, instrumentationConfig, urls);
  }

  private static class EnvKey {
    private final InstrumentationConfiguration instrumentationConfiguration;
    private final SdkConfig sdkConfig;
    private final boolean enableRendering;

    public EnvKey(
        InstrumentationConfiguration instrumentationConfiguration,
        SdkConfig sdkConfig, boolean enableRendering) {
      this.instrumentationConfiguration = instrumentationConfiguration;
      this.sdkConfig = sdkConfig;
      this.enableRendering = enableRendering;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      EnvKey envKey = (EnvKey) o;

      if (enableRendering != envKey.enableRendering) {
        return false;
      }
      if (!instrumentationConfiguration.equals(envKey.instrumentationConfiguration)) {
        return false;
      }
      return sdkConfig.equals(envKey.sdkConfig);
    }

    @Override
    public int hashCode() {
      int result = instrumentationConfiguration.hashCode();
      result = 31 * result + sdkConfig.hashCode();
      result = 31 * result + (enableRendering ? 1 : 0);
      return result;
    }
  }

}
