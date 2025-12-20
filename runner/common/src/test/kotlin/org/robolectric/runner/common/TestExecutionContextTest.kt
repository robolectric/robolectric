package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.annotation.Config

/** Tests for [TestExecutionContext]. */
class TestExecutionContextTest {

  @Test
  fun `TestClass testMethod exists for reflection`() {
    val method = TestClass::class.java.getMethod("testMethod")
    assertThat(method).isNotNull()
    assertThat(method.name).isEqualTo("testMethod")
  }

  @Test
  fun `TestClass is annotated with Config`() {
    val config = TestClass::class.java.getAnnotation(Config::class.java)
    assertThat(config).isNotNull()
    assertThat(config.sdk).asList().contains(29)
  }

  @Config(sdk = [29])
  class TestClass {
    fun testMethod() {
      // Test method for reflection
    }
  }
}
