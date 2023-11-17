package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.experimental.LazyApplication.LazyLoad.OFF;
import static org.robolectric.annotation.experimental.LazyApplication.LazyLoad.ON;

import java.util.Properties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test for {@link LazyApplicationConfigurer} */
@RunWith(JUnit4.class)
public class LazyApplicationConfigurerTest {

  private LazyApplicationConfigurer configurer =
      new LazyApplicationConfigurer(new Properties(), new PackagePropertiesLoader());

  @Test
  public void merge_explicitChildConfigOverridesParent() {
    assertThat(configurer.merge(ON, OFF)).isEqualTo(OFF);
    assertThat(configurer.merge(OFF, ON)).isEqualTo(ON);
  }
}
