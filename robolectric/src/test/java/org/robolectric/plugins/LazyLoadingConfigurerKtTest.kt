package org.robolectric.plugins

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.LazyLoadApplication
import org.robolectric.annotation.LazyLoadApplication.LazyLoad
import org.robolectric.junit.rules.BackgroundTestRule

@RunWith(AndroidJUnit4::class)
class LazyLoadingConfigurerKtTest {

  private var configurer = LazyLoadingConfigurer()

  @Test
  fun getConfigForClass_withBackgroundTestRule_returnsOffByDefault() {
    assertThat(configurer.getConfigFor(BackgroundTestRuleTest::class.java))
      .isEqualTo(LazyLoad.OFF)
  }

  @Test
  fun getConfigForClass_withBackgroundTestRule_overriddenByAnnotation() {
    assertThat(configurer.getConfigFor(BackgroundTestRuleLazyTest::class.java))
      .isEqualTo(LazyLoad.ON)
  }

  @Ignore("Dummy test class for LazyLoadingConfigurerKtTest")
  private class BackgroundTestRuleTest {
    @get:Rule
    var rule = BackgroundTestRule()

    @Test
    fun test() = Unit
  }

  @Ignore("Dummy test class for LazyLoadingConfigurerKtTest")
  @LazyLoadApplication(LazyLoad.ON)
  private class BackgroundTestRuleLazyTest {
    @get:Rule
    var rule = BackgroundTestRule()

    @Test
    fun testLazyLoadOverride() = Unit
  }
}
