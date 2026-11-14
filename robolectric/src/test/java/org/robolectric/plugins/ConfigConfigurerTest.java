package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.pluginapi.config.GlobalConfigProvider;
import org.robolectric.util.inject.Injector;

@RunWith(JUnit4.class)
public class ConfigConfigurerTest {

  @Test
  public void withoutAGlobalConfigProvider_injectsTheDefaults() {
    ConfigConfigurer configurer =
        new Injector.Builder().build().getInstance(ConfigConfigurer.class);

    // Config.Implementation doesn't implement equals().
    assertThat(configurer.defaultConfig().toString())
        .isEqualTo(Config.Builder.defaults().build().toString());
  }

  @Test
  public void withGlobalConfigProviders_usesTheFirst() {
    GlobalConfigProvider first = () -> new Config.Builder().setSdk(30).build();
    GlobalConfigProvider second = () -> new Config.Builder().setSdk(31).build();

    ConfigConfigurer configurer =
        new ConfigConfigurer(
            new PackagePropertiesLoader(), new GlobalConfigProvider[] {first, second});

    assertThat(configurer.defaultConfig().sdk()).asList().containsExactly(30);
  }
}
