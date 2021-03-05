package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LazyLoadApplication.LazyLoad.OFF;
import static org.robolectric.annotation.LazyLoadApplication.LazyLoad.ON;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.LazyLoadApplication;
import org.robolectric.annotation.LazyLoadApplication.LazyLoad;
import org.robolectric.junit.rules.BackgroundTestRule;

/** Unit test for {@link LazyLoadingConfigurer} */
@RunWith(JUnit4.class)
public class LazyLoadingConfigurerTest {

  private LazyLoadingConfigurer configurer = new LazyLoadingConfigurer();

  @Test
  public void merge_explicitChildConfigOverridesParent() {
    assertThat(configurer.merge(ON, OFF)).isEqualTo(OFF);
    assertThat(configurer.merge(OFF, ON)).isEqualTo(ON);
  }

  @Test
  public void getConfigForClass_withBackgroundTestRule_returnsOffByDefault() {
    assertThat(configurer.getConfigFor(BackgroundTestRuleTest.class)).isEqualTo(OFF);
  }

  @Test
  public void getConfigForClass_withBackgroundTestRule_overriddenByAnnotation() {
    assertThat(configurer.getConfigFor(BackgroundTestRuleLazyTest.class)).isEqualTo(ON);
  }

  @Ignore("Dummy test class for LazyLoadingConfigurerTest")
  private static class BackgroundTestRuleTest {

    @Rule BackgroundTestRule rule = new BackgroundTestRule();

    @Test
    public void test() {}
  }

  @Ignore("Dummy test class for LazyLoadingConfigurerTest")
  @LazyLoadApplication(LazyLoad.ON)
  private static class BackgroundTestRuleLazyTest {

    @Rule BackgroundTestRule rule = new BackgroundTestRule();

    @Test
    public void testLazyLoadOverride() {}
  }
}
