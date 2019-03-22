package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;

import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.LooperMode;

/**
 * Unit tests for methods annotated with @LooperMode.
 */
@RunWith(JUnit4.class)
public class LooperModeConfigurerTest {

  @Test
  public void defaultConfig() {
    Properties systemProperties = new Properties();
    LooperModeConfigurer configurer = new LooperModeConfigurer(systemProperties);
    assertThat(configurer.defaultConfig()).isSameAs(LooperMode.Mode.LEGACY);

    systemProperties.setProperty("robolectric.looperMode", "LEGACY");
    assertThat(configurer.defaultConfig()).isSameAs(LooperMode.Mode.LEGACY);

    systemProperties.setProperty("robolectric.looperMode", "RUNNING");
    assertThat(configurer.defaultConfig()).isSameAs(LooperMode.Mode.RUNNING);

    systemProperties.setProperty("robolectric.looperMode", "PAUSED");
    assertThat(configurer.defaultConfig()).isSameAs(LooperMode.Mode.PAUSED);
  }
}
