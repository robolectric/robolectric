package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;

import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;

/** Unit tests for methods annotated with @{@link GraphicsMode}. */
@RunWith(JUnit4.class)
public class GraphicsModeConfigurerTest {
  @Test
  public void defaultConfig() {
    Properties systemProperties = new Properties();
    GraphicsModeConfigurer configurer = new GraphicsModeConfigurer(systemProperties);
    assertThat(configurer.defaultConfig()).isSameInstanceAs(Mode.LEGACY);
  }
}
