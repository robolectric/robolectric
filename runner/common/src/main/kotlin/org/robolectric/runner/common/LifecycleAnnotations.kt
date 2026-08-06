package org.robolectric.runner.common
/**
 * Predefined lifecycle annotation sets for common testing frameworks.
 *
 * The annotation classes are resolved at runtime to avoid compile-time dependencies on specific
 * testing frameworks.
 */
@ExperimentalRunnerApi
enum class LifecycleAnnotations(
  private val beforeEachNames: List<String>,
  private val afterEachNames: List<String>,
  private val beforeAllNames: List<String>,
  private val afterAllNames: List<String>,
) {
  JUNIT4(
    beforeEachNames = listOf("org.junit.Before"),
    afterEachNames = listOf("org.junit.After"),
    beforeAllNames = listOf("org.junit.BeforeClass"),
    afterAllNames = listOf("org.junit.AfterClass"),
  ),
  JUNIT5(
    beforeEachNames = listOf("org.junit.jupiter.api.BeforeEach"),
    afterEachNames = listOf("org.junit.jupiter.api.AfterEach"),
    beforeAllNames = listOf("org.junit.jupiter.api.BeforeAll"),
    afterAllNames = listOf("org.junit.jupiter.api.AfterAll"),
  ),
  JUNIT_COMBINED(
    beforeEachNames = listOf("org.junit.Before", "org.junit.jupiter.api.BeforeEach"),
    afterEachNames = listOf("org.junit.After", "org.junit.jupiter.api.AfterEach"),
    beforeAllNames = listOf("org.junit.BeforeClass", "org.junit.jupiter.api.BeforeAll"),
    afterAllNames = listOf("org.junit.AfterClass", "org.junit.jupiter.api.AfterAll"),
  ),
  NONE(
    beforeEachNames = emptyList(),
    afterEachNames = emptyList(),
    beforeAllNames = emptyList(),
    afterAllNames = emptyList(),
  );

  /** Resolved annotation classes for per-test setup methods. */
  val beforeEachAnnotations: List<Class<out Annotation>> by lazy {
    resolveAnnotations(beforeEachNames)
  }
  /** Resolved annotation classes for per-test teardown methods. */
  val afterEachAnnotations: List<Class<out Annotation>> by lazy {
    resolveAnnotations(afterEachNames)
  }
  /** Resolved annotation classes for per-class setup methods. */
  val beforeAllAnnotations: List<Class<out Annotation>> by lazy {
    resolveAnnotations(beforeAllNames)
  }
  /** Resolved annotation classes for per-class teardown methods. */
  val afterAllAnnotations: List<Class<out Annotation>> by lazy { resolveAnnotations(afterAllNames) }

  @Suppress("UNCHECKED_CAST")
  private fun resolveAnnotations(names: List<String>): List<Class<out Annotation>> {
    return names.mapNotNull { name ->
      try {
        Class.forName(name) as Class<out Annotation>
      } catch (_: ClassNotFoundException) {
        null
      }
    }
  }

  companion object {
    @JvmStatic
    fun custom(
      beforeEach: List<Class<out Annotation>> = emptyList(),
      afterEach: List<Class<out Annotation>> = emptyList(),
      beforeAll: List<Class<out Annotation>> = emptyList(),
      afterAll: List<Class<out Annotation>> = emptyList(),
    ): CustomLifecycleAnnotations {
      return CustomLifecycleAnnotations(beforeEach, afterEach, beforeAll, afterAll)
    }
  }
}

@ExperimentalRunnerApi
data class CustomLifecycleAnnotations(
  val beforeEachAnnotations: List<Class<out Annotation>>,
  val afterEachAnnotations: List<Class<out Annotation>>,
  val beforeAllAnnotations: List<Class<out Annotation>>,
  val afterAllAnnotations: List<Class<out Annotation>>,
)
