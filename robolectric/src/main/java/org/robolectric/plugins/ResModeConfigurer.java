package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.ConfiguredTest;
import org.robolectric.pluginapi.config.Configurer;

@AutoService(Configurer.class)
public class ResModeConfigurer implements Configurer<ResourcesMode> {

  public static final ResModeStrategy STRATEGY = ResModeStrategy.getFromProperties();

  @Override
  public Class<ResourcesMode> getConfigClass() {
    return ResourcesMode.class;
  }

  @Nonnull
  @Override
  public ResourcesMode defaultConfig() {
    return ResourcesMode.BINARY;
  }

  @Override
  public ResourcesMode getConfigFor(@Nonnull String packageName) {
    return null;
  }

  @Override
  public ResourcesMode getConfigFor(@Nonnull Class<?> testClass) {
    return null;
  }

  @Override
  public ResourcesMode getConfigFor(@Nonnull Method method) {
    return null;
  }

  @Nonnull
  @Override
  public ResourcesMode merge(@Nonnull ResourcesMode parentConfig,
      @Nonnull ResourcesMode childConfig) {
    return childConfig;
  }

  @Override
  public List<ConfiguredTest> reconfigureAndMaybeExpandOrFilterTest(ConfiguredTest test) {
    AndroidManifest appManifest = test.getConfiguration().get(AndroidManifest.class);

    List<ConfiguredTest> configuredTests = new ArrayList<>();
    if (STRATEGY.includeLegacy(appManifest)) {
      configuredTests.add(buildConfiguredTest(test, ResourcesMode.LEGACY));
    }

    if (STRATEGY.includeBinary(appManifest)) {
      configuredTests.add(buildConfiguredTest(test, ResourcesMode.BINARY));
    }

    return configuredTests;
  }

  private ConfiguredTest buildConfiguredTest(ConfiguredTest test, ResourcesMode resourcesMode) {
    ConfiguredTest.Builder builder = test.newBuilder()
        .put(ResourcesMode.class, resourcesMode);
    if (STRATEGY == ResModeStrategy.both) {
      builder.appendToName("[" + resourcesMode + "]");
    }
    return builder.build();
  }

  public enum ResModeStrategy {
    legacy,
    binary,
    best,
    both;

    static final ResModeStrategy DEFAULT = best;

    public static ResModeStrategy getFromProperties() {
      String resourcesMode = System.getProperty("robolectric.resourcesMode");
      return resourcesMode == null ? DEFAULT : valueOf(resourcesMode);
    }

    public boolean includeLegacy(AndroidManifest appManifest) {
      return appManifest.supportsLegacyResourcesMode()
          &&
          (this == legacy
              || (this == best && !appManifest.supportsBinaryResourcesMode())
              || this == both);
    }

    public boolean includeBinary(AndroidManifest appManifest) {
      return appManifest.supportsBinaryResourcesMode()
          && (this == binary || this == best || this == both);
    }
  }
}
