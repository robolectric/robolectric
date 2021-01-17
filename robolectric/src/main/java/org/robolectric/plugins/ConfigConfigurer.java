package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.pluginapi.config.GlobalConfigProvider;

/** Provides configuration to Robolectric for its &#064;{@link Config} annotation. */
@AutoService(Configurer.class)
public class ConfigConfigurer implements Configurer<Config> {

  private final PackagePropertiesLoader packagePropertiesLoader;
  private final Config defaultConfig;

  public static Config get(Configuration testConfig) {
    return testConfig.get(Config.class);
  }

  protected ConfigConfigurer(PackagePropertiesLoader packagePropertiesLoader) {
    this(packagePropertiesLoader, () -> new Config.Builder().build());
  }

  public ConfigConfigurer(
      PackagePropertiesLoader packagePropertiesLoader,
      GlobalConfigProvider defaultConfigProvider) {
    this.packagePropertiesLoader = packagePropertiesLoader;
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

}
