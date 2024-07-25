package org.robolectric.internal;

import android.annotation.SuppressLint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.inject.AutoFactory;

/** Manager of sandboxes. */
@SuppressLint("NewApi")
public class SandboxManager {

  /** The factor for cache size. See {@link #sandboxesByKey} for details. */
  private static final int CACHE_SIZE_FACTOR = 3;

  private final SandboxBuilder sandboxBuilder;
  private final SdkCollection sdkCollection;

  // Simple LRU Cache. AndroidSandboxes are unique across InstrumentationConfiguration and Sdk
  private final LinkedHashMap<SandboxKey, AndroidSandbox> sandboxesByKey;

  @Inject
  public SandboxManager(SandboxBuilder sandboxBuilder, SdkCollection sdkCollection) {
    this.sandboxBuilder = sandboxBuilder;
    this.sdkCollection = sdkCollection;

    // We need to set the cache size of class loaders more than the number of supported APIs as
    // different tests may have different configurations.
    final int cacheSize = sdkCollection.getSupportedSdks().size() * CACHE_SIZE_FACTOR;
    sandboxesByKey =
        new LinkedHashMap<SandboxKey, AndroidSandbox>() {
          @Override
          protected boolean removeEldestEntry(Map.Entry<SandboxKey, AndroidSandbox> eldest) {
            boolean toRemove = size() > cacheSize;
            if (toRemove) {
              PerfStatsCollector.getInstance().incrementCount("evictSandbox");
              eldest.getValue().shutdown();
            }
            return toRemove;
          }
        };
  }

  public synchronized AndroidSandbox getAndroidSandbox(
      InstrumentationConfiguration instrumentationConfig,
      Sdk sdk,
      ResourcesMode.Mode resourcesMode,
      LooperMode.Mode looperMode,
      SQLiteMode.Mode sqliteMode,
      GraphicsMode.Mode graphicsMode) {
    SandboxKey key =
        new SandboxKey(instrumentationConfig, sdk, resourcesMode, looperMode, graphicsMode);

    AndroidSandbox androidSandbox = sandboxesByKey.get(key);
    if (androidSandbox == null) {
      Sdk compileSdk = sdkCollection.getMaxSupportedSdk();
      androidSandbox =
          sandboxBuilder.build(instrumentationConfig, sdk, compileSdk, resourcesMode, sqliteMode);
      sandboxesByKey.put(key, androidSandbox);
    }
    androidSandbox.updateModes(sqliteMode);
    return androidSandbox;
  }

  /** Factory interface for AndroidSandbox. */
  @AutoFactory
  public interface SandboxBuilder {
    AndroidSandbox build(
        InstrumentationConfiguration instrumentationConfig,
        @Named("runtimeSdk") Sdk runtimeSdk,
        @Named("compileSdk") Sdk compileSdk,
        ResourcesMode.Mode resourcesMode,
        SQLiteMode.Mode sqLiteMode);
  }

  static class SandboxKey {
    private final Sdk sdk;
    private final InstrumentationConfiguration instrumentationConfiguration;
    private final ResourcesMode.Mode resourcesMode;
    private final LooperMode.Mode looperMode;
    private final GraphicsMode.Mode graphicsMode;

    public SandboxKey(
        InstrumentationConfiguration instrumentationConfiguration,
        Sdk sdk,
        ResourcesMode.Mode resourcesMode,
        LooperMode.Mode looperMode,
        GraphicsMode.Mode graphicsMode) {
      this.sdk = sdk;
      this.instrumentationConfiguration = instrumentationConfiguration;
      this.resourcesMode = resourcesMode;
      this.looperMode = looperMode;
      this.graphicsMode = graphicsMode;
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
          && Objects.equals(instrumentationConfiguration, that.instrumentationConfiguration)
          && looperMode == that.looperMode
          && graphicsMode == that.graphicsMode;
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          sdk, instrumentationConfiguration, resourcesMode, looperMode, graphicsMode);
    }
  }
}
