package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.UsesSdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.pluginapi.config.ConfiguredTest;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.pluginapi.config.GlobalConfigProvider;

/** Provides configuration to Robolectric for its `@`{@link Config} annotation. */
@AutoService(Configurer.class)
public class ConfigConfigurer implements Configurer<Config> {

  private final PackagePropertiesLoader packagePropertiesLoader;
  private final SdkPicker sdkPicker;
  private final Config defaultConfig;

  public static Config get(Configuration testConfig) {
    return testConfig.get(Config.class);
  }

  protected ConfigConfigurer(PackagePropertiesLoader packagePropertiesLoader, SdkPicker sdkPicker) {
    this(packagePropertiesLoader, sdkPicker, () -> new Config.Builder().build());
  }

  public ConfigConfigurer(
      PackagePropertiesLoader packagePropertiesLoader,
      SdkPicker sdkPicker, GlobalConfigProvider defaultConfigProvider) {
    this.packagePropertiesLoader = packagePropertiesLoader;
    this.sdkPicker = sdkPicker;
    this.defaultConfig = Config.Builder.defaults().overlay(defaultConfigProvider.get()).build();
  }

  @Override
  public Class<Config> getConfigClass() {
    return Config.class;
  }

  @Nonnull
  @Override
  public Config defaultConfig() {
    return defaultConfig;
  }

  @Override
  public Config getConfigFor(@Nonnull String packageName) {
    Properties properties = packagePropertiesLoader.getConfigProperties(packageName);
    return Config.Implementation.fromProperties(properties);
  }

  @Override
  public Config getConfigFor(@Nonnull Class<?> testClass) {
    return testClass.getAnnotation(Config.class);
  }

  @Override
  public Config getConfigFor(@Nonnull Method method) {
    return method.getAnnotation(Config.class);
  }

  @Nonnull
  @Override
  public Config merge(@Nonnull Config parentConfig, @Nonnull Config childConfig) {
    return new Config.Builder(parentConfig).overlay(childConfig).build();
  }

  // TODO: need a better name
  @Override
  public List<ConfiguredTest> reconfigureAndMaybeExpandOrFilterTest(ConfiguredTest test) {
    Configuration configuration = test.getConfiguration();
    ArrayList<ConfiguredTest> configuredTests = new ArrayList<>();
    List<Sdk> sdksToRun = sdkPicker.selectSdks(configuration, configuration.get(UsesSdk.class));
    for (Sdk sdk : sdksToRun) {
      configuredTests.add(test.newBuilder()
          .put(Sdk.class, sdk)
          .appendToName("[" + sdk.getApiLevel() + "]")
          .build());
    }
    return configuredTests;
  }
}
