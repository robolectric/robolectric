package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
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

  @Test
  fun `discoverTestMethods includes inherited methods by default`() {
    val methods =
      DiscoveryHelpers.discoverTestMethods(
        ChildDiscoveryFixture::class.java,
        listOf(org.junit.Test::class.java, org.junit.jupiter.api.Test::class.java),
      )

    assertThat(methods.map { it.name }).containsExactly("childJunit4", "childJupiter", "parentTest")
  }

  @Test
  fun `discoverTestMethods can exclude inherited methods`() {
    val methods =
      DiscoveryHelpers.discoverTestMethods(
        ChildDiscoveryFixture::class.java,
        listOf(org.junit.Test::class.java, org.junit.jupiter.api.Test::class.java),
        includeInherited = false,
      )

    assertThat(methods.map { it.name }).containsExactly("childJunit4", "childJupiter")
  }

  @Test
  fun `discoverTestMethodsByName supports mixed junit annotations`() {
    val methods =
      DiscoveryHelpers.discoverTestMethodsByName(
        ChildDiscoveryFixture::class.java,
        listOf("org.junit.Test", "org.junit.jupiter.api.Test"),
      )

    assertThat(methods.map { it.name }).containsExactly("childJunit4", "childJupiter", "parentTest")
  }

  @Test
  fun `discoverNestedClasses without annotation excludes static nested classes`() {
    val nested = DiscoveryHelpers.discoverNestedClasses(NestedDiscoveryFixture::class.java)

    assertThat(nested.map { it.simpleName }).containsExactly("InnerClass", "InnerNestedClass")
  }

  @Test
  fun `discoverNestedClasses with nested annotation includes annotated static and inner classes`() {
    val nested =
      DiscoveryHelpers.discoverNestedClasses(NestedDiscoveryFixture::class.java, Nested::class.java)

    assertThat(nested.map { it.simpleName })
      .containsExactly("InnerNestedClass", "StaticNestedClass")
  }
}

// Test fixture class (outside the test class to avoid JUnit nesting issues)
@Suppress("unused", "EmptyFunctionBlock")
class TestClassForDiscovery {
  @org.junit.jupiter.api.Test fun jupiterAnnotatedMethod() {}

  @org.junit.Test fun junit4AnnotatedMethod() {}

  fun nonTestMethod() {}
}

@Suppress("unused", "EmptyFunctionBlock")
open class ParentDiscoveryFixture {
  @org.junit.Test fun parentTest() {}
}

@Suppress("unused", "EmptyFunctionBlock")
class ChildDiscoveryFixture : ParentDiscoveryFixture() {
  @org.junit.jupiter.api.Test fun childJupiter() {}

  @org.junit.Test fun childJunit4() {}

  fun helperMethod() {}
}

@Suppress("unused", "EmptyFunctionBlock")
class NestedDiscoveryFixture {
  @Nested class StaticNestedClass

  inner class InnerClass

  @Nested inner class InnerNestedClass
}
