package org.robolectric.runner.common

import java.lang.reflect.Method

/**
 * Interface for filtering test methods during discovery.
 *
 * Implement this interface to customize which tests are included in test execution.
 */
@ExperimentalRunnerApi
fun interface TestFilter {
  /**
   * Determines if a test should be run.
   *
   * @param testClass The class containing the test
   * @param testMethod The test method
   * @return true if the test should be executed, false to skip
   */
  fun shouldRun(testClass: Class<*>, testMethod: Method): Boolean

  companion object {
    /** Filter that accepts all tests. */
    @JvmField val ACCEPT_ALL: TestFilter = TestFilter { _, _ -> true }

    /** Creates a filter that only runs tests matching the given method name pattern. */
    @JvmStatic
    fun byMethodName(pattern: Regex): TestFilter = TestFilter { _, method ->
      pattern.matches(method.name)
    }

    /** Creates a filter that only runs tests in classes matching the given pattern. */
    @JvmStatic
    fun byClassName(pattern: Regex): TestFilter = TestFilter { testClass, _ ->
      pattern.matches(testClass.name)
    }

    /** Creates a filter that only runs tests annotated with the given annotation. */
    @JvmStatic
    fun byAnnotation(annotationClass: Class<out Annotation>): TestFilter = TestFilter { _, method ->
      method.isAnnotationPresent(annotationClass)
    }

    /** Combines multiple filters with AND logic (all must pass). */
    @JvmStatic
    fun allOf(vararg filters: TestFilter): TestFilter = TestFilter { testClass, method ->
      filters.all { it.shouldRun(testClass, method) }
    }

    /** Combines multiple filters with OR logic (any must pass). */
    @JvmStatic
    fun anyOf(vararg filters: TestFilter): TestFilter = TestFilter { testClass, method ->
      filters.any { it.shouldRun(testClass, method) }
    }

    /** Negates a filter. */
    @JvmStatic
    fun not(filter: TestFilter): TestFilter = TestFilter { testClass, method ->
      !filter.shouldRun(testClass, method)
    }
  }
}
