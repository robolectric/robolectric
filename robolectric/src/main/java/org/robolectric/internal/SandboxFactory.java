package org.robolectric.internal;

import android.annotation.SuppressLint;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Named;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.util.inject.AutoFactory;

@SuppressLint("NewApi")
public class SandboxFactory {

  /** The factor for cache size. See {@link #sdkToEnvironment} for details. */
  private static final int CACHE_SIZE_FACTOR = 3;

  private final SdkCollection sdkCollection;
  private final SdkEnvironmentFactory sdkEnvironmentFactory;

  // Simple LRU Cache. SdkEnvironments are unique across InstrumentationConfiguration and Sdk
  private final LinkedHashMap<SandboxKey, SdkEnvironment> sdkToEnvironment;

  public SandboxFactory(SdkCollection sdkCollection, SdkEnvironmentFactory sdkEnvironmentFactory) {
    this.sdkCollection = sdkCollection;
    this.sdkEnvironmentFactory = sdkEnvironmentFactory;

    // We need to set the cache size of class loaders more than the number of supported APIs as
    // different tests may have different configurations.
    final int cacheSize = sdkCollection.getSupportedSdks().size() * CACHE_SIZE_FACTOR;
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
      URL[] urls = new URL[]{
          asUrl(sdk.getJarPath())
      };

      ClassLoader robolectricClassLoader = createClassLoader(instrumentationConfig, urls);
      sdkEnvironment =
          sdkEnvironmentFactory.create(sdk, robolectricClassLoader, sdkCollection.getMaxSupportedSdk(),
              instrumentationConfig.getInterceptors());

      sdkToEnvironment.put(key, sdkEnvironment);
    }
    return sdkEnvironment;
  }

  @AutoFactory
  public interface SdkEnvironmentFactory {
    SdkEnvironment create(
        @Named("runtime") Sdk sdk,
        ClassLoader robolectricClassLoader,
        @Named("compileTime") Sdk compileTimeSdk,
        Interceptors interceptors);
  }

  private URL asUrl(Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
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
