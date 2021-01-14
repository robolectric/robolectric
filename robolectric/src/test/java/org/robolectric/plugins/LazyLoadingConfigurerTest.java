package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LazyLoadApplication.LazyLoad.OFF;
import static org.robolectric.annotation.LazyLoadApplication.LazyLoad.ON;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test for {@link LazyLoadingConfigurer} */
@RunWith(JUnit4.class)
public class LazyLoadingConfigurerTest {

  private LazyLoadingConfigurer configurer = new LazyLoadingConfigurer();

  @Test
  public void merge_explicitChildConfigOverridesParent() {
    assertThat(configurer.merge(ON, OFF)).isEqualTo(OFF);
    assertThat(configurer.merge(OFF, ON)).isEqualTo(ON);
  }
}
