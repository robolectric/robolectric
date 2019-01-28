package org.robolectric.internal;

import android.annotation.SuppressLint;
import com.google.common.annotations.VisibleForTesting;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.robolectric.android.internal.ParallelUniverse;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.util.inject.Injector;

@SuppressLint("NewApi")
public class SandboxFactory {

  /**
   * The factor for cache size. See {@link #sdkToEnvironment} for details.
   */
  private static final int CACHE_SIZE_FACTOR = 3;

  private final Injector injector;
  private final SdkCollection sdkCollection;

  // Simple LRU Cache. SdkEnvironments are unique across InstrumentationConfiguration and Sdk
  private final LinkedHashMap<SandboxKey, SdkEnvironment> sdkToEnvironment;

  @Inject
  public SandboxFactory(Injector injector, SdkCollection sdkCollection) {
    this.injector = injector;
    this.sdkCollection = sdkCollection;

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
      Sdk sdk, ResourcesMode resourcesMode) {
    SandboxKey key = new SandboxKey(instrumentationConfig, sdk, resourcesMode);

    SdkEnvironment sdkEnvironment = sdkToEnvironment.get(key);
    if (sdkEnvironment == null) {
      URL[] urls = new URL[]{
          asUrl(sdk.getJarPath())
      };

      ClassLoader robolectricClassLoader = createClassLoader(instrumentationConfig, urls);
      sdkEnvironment = createSdkEnvironment(sdk, robolectricClassLoader, resourcesMode,
          sdkCollection.getMaxSupportedSdk());

      sdkToEnvironment.put(key, sdkEnvironment);
    }
    return sdkEnvironment;
  }

  private URL asUrl(Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private SdkEnvironment createSdkEnvironment(
      Sdk sdk, ClassLoader robolectricClassLoader, ResourcesMode resourcesMode, Sdk compileSdk) {

    Injector sandboxScope =
        injector.newScopeBuilder(robolectricClassLoader)
            .bind(ParallelUniverseInterface.class, bootstrap(robolectricClassLoader))
            .bind(new Injector.Key<>(Sdk.class, "runtimeSdk"), sdk)
            .bind(new Injector.Key<>(Sdk.class, "compileSdk"), compileSdk)
            .bind(ResourcesMode.class, resourcesMode)
            .build();

    ParallelUniverseInterface parallelUniverseInterface =
        sandboxScope.getInstance(ParallelUniverseInterface.class);

    return new SdkEnvironment(parallelUniverseInterface, robolectricClassLoader, sdk);
  }

  private Class<? extends ParallelUniverseInterface> bootstrap(ClassLoader robolectricClassLoader) {
    try {
      return robolectricClassLoader
          .loadClass(getParallelUniverseClass().getName())
          .asSubclass(ParallelUniverseInterface.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @VisibleForTesting
  protected Class<? extends ParallelUniverseInterface> getParallelUniverseClass() {
    return ParallelUniverse.class;
  }

  @Nonnull
  public ClassLoader createClassLoader(
      InstrumentationConfiguration instrumentationConfig, URL... urls) {
    return new SandboxClassLoader(ClassLoader.getSystemClassLoader(), instrumentationConfig, urls);
  }

  static class SandboxKey {
    private final Sdk sdk;
    private final InstrumentationConfiguration instrumentationConfiguration;
    private final ResourcesMode resourcesMode;

    public SandboxKey(
        InstrumentationConfiguration instrumentationConfiguration, Sdk sdk,
        ResourcesMode resourcesMode) {
      this.sdk = sdk;
      this.instrumentationConfiguration = instrumentationConfiguration;
      this.resourcesMode = resourcesMode;
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
      return resourcesMode == that.resourcesMode
          && Objects.equals(sdk, that.sdk)
          && Objects.equals(instrumentationConfiguration, that.instrumentationConfiguration);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sdk, instrumentationConfiguration, resourcesMode);
    }
  }
}
