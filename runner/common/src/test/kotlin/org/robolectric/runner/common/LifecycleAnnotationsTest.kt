package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class LifecycleAnnotationsTest {
  @Test
  fun `JUNIT4 resolves JUnit4 annotations`() {
    val annotations = LifecycleAnnotations.JUNIT4
    assertThat(annotations.beforeEachAnnotations).contains(Before::class.java)
    assertThat(annotations.afterEachAnnotations).contains(org.junit.After::class.java)
    assertThat(annotations.beforeAllAnnotations).contains(BeforeClass::class.java)
    assertThat(annotations.afterAllAnnotations).contains(org.junit.AfterClass::class.java)
  }

  @Test
  fun `JUNIT5 resolves JUnit5 annotations when available`() {
    val annotations = LifecycleAnnotations.JUNIT5
    // Jupiter classes are available in test classpath
    assertThat(annotations.beforeEachAnnotations).isNotEmpty()
    assertThat(annotations.afterEachAnnotations).isNotEmpty()
    assertThat(annotations.beforeAllAnnotations).isNotEmpty()
    assertThat(annotations.afterAllAnnotations).isNotEmpty()
  }

  @Test
  fun `JUNIT_COMBINED contains both JUnit4 and JUnit5 annotations`() {
    val annotations = LifecycleAnnotations.JUNIT_COMBINED
    // Should contain at least JUnit 4 annotations
    assertThat(annotations.beforeEachAnnotations).contains(Before::class.java)
    assertThat(annotations.beforeAllAnnotations).contains(BeforeClass::class.java)
    // And more when Jupiter is available
    assertThat(annotations.beforeEachAnnotations.size).isAtLeast(1)
  }

  @Test
  fun `NONE has empty annotation lists`() {
    val annotations = LifecycleAnnotations.NONE
    assertThat(annotations.beforeEachAnnotations).isEmpty()
    assertThat(annotations.afterEachAnnotations).isEmpty()
    assertThat(annotations.beforeAllAnnotations).isEmpty()
    assertThat(annotations.afterAllAnnotations).isEmpty()
  }

  @Test
  fun `custom creates CustomLifecycleAnnotations`() {
    val custom =
      LifecycleAnnotations.custom(
        beforeEach = listOf(Before::class.java),
        afterEach = listOf(org.junit.After::class.java),
      )
    assertThat(custom.beforeEachAnnotations).containsExactly(Before::class.java)
    assertThat(custom.afterEachAnnotations).containsExactly(org.junit.After::class.java)
    assertThat(custom.beforeAllAnnotations).isEmpty()
    assertThat(custom.afterAllAnnotations).isEmpty()
  }

  @Test
  fun `all values are present`() {
    val values = LifecycleAnnotations.entries
    assertThat(values).hasSize(4)
    assertThat(values.map { it.name }).containsExactly("JUNIT4", "JUNIT5", "JUNIT_COMBINED", "NONE")
  }
}
