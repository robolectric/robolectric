package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/** Tests for [DiscoveryHelpers] utility functions. */
@OptIn(ExperimentalRunnerApi::class)
class DiscoveryHelpersTest {

  @Test
  fun `isTestMethod returns true for JUnit Jupiter @Test`() {
    val method = TestClassForDiscovery::class.java.getDeclaredMethod("jupiterAnnotatedMethod")
    assertThat(DiscoveryHelpers.isTestMethod(method)).isTrue()
  }

  @Test
  fun `isTestMethod returns true for JUnit 4 @Test`() {
    val method = TestClassForDiscovery::class.java.getDeclaredMethod("junit4AnnotatedMethod")
    assertThat(DiscoveryHelpers.isTestMethod(method)).isTrue()
  }

  @Test
  fun `isTestMethod returns false for non-test methods`() {
    val method = TestClassForDiscovery::class.java.getDeclaredMethod("nonTestMethod")
    assertThat(DiscoveryHelpers.isTestMethod(method)).isFalse()
  }

  @Test
  fun `isTestMethod handles methods from Object class`() {
    val method = Object::class.java.getMethod("toString")
    assertThat(DiscoveryHelpers.isTestMethod(method)).isFalse()
  }
}

// Test fixture class (outside the test class to avoid JUnit nesting issues)
@Suppress("unused", "EmptyFunctionBlock")
class TestClassForDiscovery {
  @org.junit.jupiter.api.Test fun jupiterAnnotatedMethod() {}

  @org.junit.Test fun junit4AnnotatedMethod() {}

  fun nonTestMethod() {}
}
