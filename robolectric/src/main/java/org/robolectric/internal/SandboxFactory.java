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
import org.robolectric.android.internal.AndroidEnvironment;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.util.inject.Injector;

@SuppressLint("NewApi")
public class SandboxFactory {

  /**
   * The factor for cache size. See {@link #sandboxesByKey} for details.
   */
  private static final int CACHE_SIZE_FACTOR = 3;

  private final Injector injector;
  private final SdkCollection sdkCollection;

  // Simple LRU Cache. AndroidSandboxes are unique across InstrumentationConfiguration and Sdk
  private final LinkedHashMap<SandboxKey, AndroidSandbox> sandboxesByKey;

  @Inject
  public SandboxFactory(Injector injector, SdkCollection sdkCollection) {
    this.injector = injector;
    this.sdkCollection = sdkCollection;

    // We need to set the cache size of class loaders more than the number of supported APIs as
    // different tests may have different configurations.
    final int cacheSize = sdkCollection.getSupportedSdks().size() * CACHE_SIZE_FACTOR;
    sandboxesByKey = new LinkedHashMap<SandboxKey, AndroidSandbox>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<SandboxKey, AndroidSandbox> eldest) {
        return size() > cacheSize;
      }
    };
  }

  public synchronized AndroidSandbox getAndroidSandbox(
      InstrumentationConfiguration instrumentationConfig,
      Sdk sdk, ResourcesMode resourcesMode) {
    SandboxKey key = new SandboxKey(instrumentationConfig, sdk, resourcesMode);

    AndroidSandbox androidSandbox = sandboxesByKey.get(key);
    if (androidSandbox == null) {
      URL[] urls = new URL[]{
          asUrl(sdk.getJarPath())
      };

      ClassLoader robolectricClassLoader = createClassLoader(instrumentationConfig, urls);
      androidSandbox = createAndroidSandbox(sdk, robolectricClassLoader, resourcesMode,
          sdkCollection.getMaxSupportedSdk());

      sandboxesByKey.put(key, androidSandbox);
    }
    return androidSandbox;
  }

  private URL asUrl(Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private AndroidSandbox createAndroidSandbox(
      Sdk sdk, ClassLoader robolectricClassLoader, ResourcesMode resourcesMode, Sdk compileSdk) {

    Injector sandboxScope =
        injector.newScopeBuilder(robolectricClassLoader)
            .bind(Environment.class, bootstrap(robolectricClassLoader))
            .bind(new Injector.Key<>(Sdk.class, "runtimeSdk"), sdk)
            .bind(new Injector.Key<>(Sdk.class, "compileSdk"), compileSdk)
            .bind(ResourcesMode.class, resourcesMode)
            .build();

    return new AndroidSandbox(() -> sandboxScope.getInstance(Environment.class),
        robolectricClassLoader, sdk);
  }

  private Class<? extends Environment> bootstrap(ClassLoader robolectricClassLoader) {
    try {
      return robolectricClassLoader
          .loadClass(getEnvironmentClass().getName())
          .asSubclass(Environment.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @VisibleForTesting
  protected Class<? extends Environment> getEnvironmentClass() {
    return AndroidEnvironment.class;
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
      if (!(o instanceof SandboxKey)) {
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
