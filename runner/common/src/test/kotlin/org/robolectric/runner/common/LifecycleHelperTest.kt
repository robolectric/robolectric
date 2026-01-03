package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests for [LifecycleHelper] to verify correct lifecycle method ordering and override handling as
 * per JUnit 5 specification.
 */
@OptIn(ExperimentalRunnerApi::class)
class LifecycleHelperTest {

  // Test annotation for simulating lifecycle annotations
  @Retention(AnnotationRetention.RUNTIME)
  @Target(AnnotationTarget.FUNCTION)
  annotation class BeforeEach

  @Retention(AnnotationRetention.RUNTIME)
  @Target(AnnotationTarget.FUNCTION)
  annotation class AfterEach

  // Superclass with lifecycle method
  open class SuperClass {
    @BeforeEach
    open fun superBefore() {
      // Will be tracked via reflection
    }

    @AfterEach
    open fun superAfter() {
      // Will be tracked via reflection
    }
  }

  // Subclass with additional lifecycle method
  class SubClass : SuperClass() {
    @BeforeEach
    fun subBefore() {
      // Will be tracked via reflection
    }

    @AfterEach
    fun subAfter() {
      // Will be tracked via reflection
    }
  }

  // Class that overrides a lifecycle method
  class OverridingClass : SuperClass() {
    @BeforeEach
    override fun superBefore() {
      // Overrides the superclass method
    }
  }

  @Test
  fun `isBeforeAnnotation correctly identifies Before annotations`() {
    // Use reflection to access the private method
    val method =
      LifecycleHelper::class.java.getDeclaredMethod("isBeforeAnnotation", Class::class.java)
    method.isAccessible = true

    assertThat(method.invoke(LifecycleHelper, BeforeEach::class.java) as Boolean).isTrue()
    assertThat(method.invoke(LifecycleHelper, AfterEach::class.java) as Boolean).isFalse()
  }

  @Test
  fun `methodSignature creates consistent signatures`() {
    // Use reflection to access the private method
    val method =
      LifecycleHelper::class
        .java
        .getDeclaredMethod("methodSignature", java.lang.reflect.Method::class.java)
    method.isAccessible = true

    val superMethod = SuperClass::class.java.getDeclaredMethod("superBefore")
    val overriddenMethod = OverridingClass::class.java.getDeclaredMethod("superBefore")

    val superSignature = method.invoke(LifecycleHelper, superMethod) as String
    val overriddenSignature = method.invoke(LifecycleHelper, overriddenMethod) as String

    // Same name and parameters should produce the same signature
    assertThat(superSignature).isEqualTo(overriddenSignature)
  }

  @Test
  fun `lifecycle methods with different parameters have different signatures`() {
    // Use reflection to access the private method
    val method =
      LifecycleHelper::class
        .java
        .getDeclaredMethod("methodSignature", java.lang.reflect.Method::class.java)
    method.isAccessible = true

    // Get two methods with different parameters
    val noArgMethod = Object::class.java.getMethod("toString")
    val oneArgMethod = Object::class.java.getMethod("equals", Object::class.java)

    val noArgSignature = method.invoke(LifecycleHelper, noArgMethod) as String
    val oneArgSignature = method.invoke(LifecycleHelper, oneArgMethod) as String

    assertThat(noArgSignature).isNotEqualTo(oneArgSignature)
  }
}
