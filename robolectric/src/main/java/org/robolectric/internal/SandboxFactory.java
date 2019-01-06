package org.robolectric.internal;

import android.annotation.SuppressLint;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.pluginapi.SdkProvider;

@SuppressLint("NewApi")
public class SandboxFactory {

  /** The factor for cache size. See {@link #sdkToEnvironment} for details. */
  private static final int CACHE_SIZE_FACTOR = 3;

  private final DependencyResolver dependencyResolver;
  private final SdkProvider sdkProvider;

  // Simple LRU Cache. SdkEnvironments are unique across InstrumentationConfiguration and Sdk
  private final LinkedHashMap<SandboxKey, SdkEnvironment> sdkToEnvironment;

  @Inject
  public SandboxFactory(DependencyResolver dependencyResolver, SdkProvider sdkProvider) {
    this.dependencyResolver = dependencyResolver;
    this.sdkProvider = sdkProvider;

    // We need to set the cache size of class loaders more than the number of supported APIs as
    // different tests may have different configurations.
    final int cacheSize = sdkProvider.getSupportedSdks().size() * CACHE_SIZE_FACTOR;
    sdkToEnvironment = new LinkedHashMap<SandboxKey, SdkEnvironment>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<SandboxKey, SdkEnvironment> eldest) {
        return size() > cacheSize;
      }
    };
  }

  public synchronized SdkEnvironment getSdkEnvironment(
      InstrumentationConfiguration instrumentationConfig,
      Sdk sdk,
      boolean useLegacyResources) {
    SandboxKey key = new SandboxKey(sdk, instrumentationConfig, useLegacyResources);

    SdkEnvironment sdkEnvironment = sdkToEnvironment.get(key);
    if (sdkEnvironment == null) {
      URL[] urls = dependencyResolver.getLocalArtifactUrls(sdk.getAndroidSdkDependency());

      ClassLoader robolectricClassLoader = createClassLoader(instrumentationConfig, urls);
      sdkEnvironment = createSdkEnvironment(sdk, robolectricClassLoader);

      sdkToEnvironment.put(key, sdkEnvironment);
    }
    return sdkEnvironment;
  }

  protected SdkEnvironment createSdkEnvironment(
      Sdk sdk, ClassLoader robolectricClassLoader) {
    return new SdkEnvironment(
        sdk, robolectricClassLoader, sdkProvider.getMaxSupportedSdk());
  }

  @Nonnull
  public ClassLoader createClassLoader(
      InstrumentationConfiguration instrumentationConfig, URL... urls) {
    return new SandboxClassLoader(ClassLoader.getSystemClassLoader(), instrumentationConfig, urls);
  }

  static class SandboxKey {
    private final Sdk sdk;
    private final InstrumentationConfiguration instrumentationConfiguration;
    private final boolean useLegacyResources;

    public SandboxKey(
        Sdk sdk,
        InstrumentationConfiguration instrumentationConfiguration,
        boolean useLegacyResources) {
      this.sdk = sdk;
      this.instrumentationConfiguration = instrumentationConfiguration;
      this.useLegacyResources = useLegacyResources;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SandboxKey that = (SandboxKey) o;
      return useLegacyResources == that.useLegacyResources
          && Objects.equals(sdk, that.sdk)
          && Objects.equals(instrumentationConfiguration, that.instrumentationConfiguration);
    }

    @Override
    public int hashCode() {

      return Objects.hash(sdk, instrumentationConfiguration, useLegacyResources);
    }
  }
}
