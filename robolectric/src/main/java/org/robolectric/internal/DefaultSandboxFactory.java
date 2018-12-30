package org.robolectric.internal;

import android.annotation.SuppressLint;
import com.google.auto.service.AutoService;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.inject.Inject;
import org.robolectric.ApkLoader;
import org.robolectric.SdkProvider;
import org.robolectric.internal.AndroidSandbox.SandboxConfig;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.util.inject.Injector;

@AutoService(SandboxFactory.class)
@Priority(Integer.MIN_VALUE)
@SuppressLint("NewApi")
public class DefaultSandboxFactory implements SandboxFactory {

  /** The factor for cache size. See {@link #sdkToEnvironment} for details. */
  private static final int CACHE_SIZE_FACTOR = 3;

  private final Injector injector;
  private final DependencyResolver dependencyResolver;
  private final ApkLoader apkLoader;

  // Simple LRU Cache. Sandboxes are unique across InstrumentationConfiguration and SdkConfig.
  private final LinkedHashMap<SandboxKey, AndroidSandbox> sdkToEnvironment;

  @Inject
  public DefaultSandboxFactory(Injector injector, DependencyResolver dependencyResolver,
      SdkProvider sdkProvider, ApkLoader apkLoader) {
    this.injector = injector;
    this.dependencyResolver = dependencyResolver;
    this.apkLoader = apkLoader;

    // We need to set the cache size of class loaders more than the number of supported APIs as
    // different tests may have different configurations.
    final int cacheSize = sdkProvider.getSupportedSdkConfigs().size() * CACHE_SIZE_FACTOR;
    sdkToEnvironment =
        new LinkedHashMap<SandboxKey, AndroidSandbox>() {
          @Override
          protected boolean removeEldestEntry(Map.Entry<SandboxKey, AndroidSandbox> eldest) {
            return size() > cacheSize;
          }
        };
  }

  @Override
  public synchronized AndroidSandbox getSandbox(
      InstrumentationConfiguration instrumentationConfig, SdkConfig sdkConfig,
      boolean useLegacyResources) {
    SandboxKey key = new SandboxKey(sdkConfig, instrumentationConfig, useLegacyResources);

    AndroidSandbox androidSandbox = sdkToEnvironment.get(key);
    if (androidSandbox == null) {
      URL[] urls = dependencyResolver.getLocalArtifactUrls(sdkConfig.getAndroidSdkDependency());

      ClassLoader robolectricClassLoader = createClassLoader(instrumentationConfig, urls);
      androidSandbox = createSandbox(sdkConfig, useLegacyResources, robolectricClassLoader,
          apkLoader);

      sdkToEnvironment.put(key, androidSandbox);
    }
    return androidSandbox;
  }

  protected AndroidSandbox createSandbox(SdkConfig sdkConfig, boolean useLegacyResources,
      ClassLoader robolectricClassLoader, ApkLoader apkLoader) {
    Injector sandboxScopedInjector = new Injector(this.injector)
        .register(SandboxConfig.class,
            new SandboxConfig(sdkConfig, useLegacyResources, robolectricClassLoader));

    return sandboxScopedInjector.get(AndroidSandbox.class);
  }

  @Nonnull
  public ClassLoader createClassLoader(
      InstrumentationConfiguration instrumentationConfig, URL... urls) {
    return new SandboxClassLoader(ClassLoader.getSystemClassLoader(), instrumentationConfig, urls);
  }

  static class SandboxKey {
    private final SdkConfig sdkConfig;
    private final InstrumentationConfiguration instrumentationConfiguration;
    private final boolean useLegacyResources;

    public SandboxKey(
        SdkConfig sdkConfig,
        InstrumentationConfiguration instrumentationConfiguration,
        boolean useLegacyResources) {
      this.sdkConfig = sdkConfig;
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
          && Objects.equals(sdkConfig, that.sdkConfig)
          && Objects.equals(instrumentationConfiguration, that.instrumentationConfiguration);
    }

    @Override
    public int hashCode() {

      return Objects.hash(sdkConfig, instrumentationConfiguration, useLegacyResources);
    }
  }
}
