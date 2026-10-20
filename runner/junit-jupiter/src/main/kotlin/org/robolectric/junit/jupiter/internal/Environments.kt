package org.robolectric.junit.jupiter.internal

import org.junit.jupiter.api.extension.ExtensionContext
import org.opentest4j.TestAbortedException
import org.robolectric.junit.jupiter.internal.Plans.NAMESPACE
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi

/**
 * Decides which Android environments a class or a test runs in, one for each SDK that is selected
 * for it, and keeps them in JUnit's stores, which close them when the class or the test is done.
 */
@OptIn(ExperimentalRunnerApi::class)
internal object Environments {
  private const val CLASS_ENVIRONMENTS = "classEnvironments"
  private const val TEST_ENVIRONMENTS = "testEnvironments"
  private const val SDK_TEST_CONFIGURATION = "sdkTestConfiguration"

  /**
   * Opens the environments that the tests of the class share, if they share them, unless they share
   * those of an enclosing class.
   */
  fun openForClass(context: ExtensionContext) {
    val testClass = context.requiredTestClass
    val configurations =
      if (Plans.sharesEnvironments(context)) Plans.of(context, testClass, null) else listOf()
    if (configurations.isEmpty()) {
      return
    }
    val store = context.getStore(NAMESPACE)
    val enclosing = store.get(CLASS_ENVIRONMENTS, TestEnvironments::class.java)
    if (enclosing == null) {
      store.put(CLASS_ENVIRONMENTS, TestEnvironments.open(Plans.session(context), configurations))
    } else {
      check(enclosing.environments.map { it.configuration } == configurations) {
        "${testClass.name} is configured differently than the class that encloses it, whose " +
          "Android environment its tests share. Configure both the same, or move it out of the " +
          "enclosing class."
      }
    }
  }

  /** Returns the environments that the tests of the class share. */
  fun ofClass(context: ExtensionContext): TestEnvironments {
    val environments =
      context.getStore(NAMESPACE).get(CLASS_ENVIRONMENTS, TestEnvironments::class.java)
        ?: error("${context.requiredTestClass.name} has no shared Android environment")
    return TestEnvironments(environments.environments, shared = true)
  }

  /** Returns the environments of the test if it has them already. */
  fun ofTest(context: ExtensionContext): TestEnvironments? =
    context.getStore(NAMESPACE).get(TEST_ENVIRONMENTS, TestEnvironments::class.java)

  /** Returns the environments of the test, opening them first if the test runs in its own. */
  fun forTest(context: ExtensionContext): TestEnvironments =
    context
      .getStore(NAMESPACE)
      .computeIfAbsent(TEST_ENVIRONMENTS, { resolveForTest(context) }, TestEnvironments::class.java)

  /** Makes the test of the context run in the given environment, as a RobolectricSdkTest does. */
  fun setSdkTestConfiguration(context: ExtensionContext, configuration: Configuration) {
    context.getStore(NAMESPACE).put(SDK_TEST_CONFIGURATION, configuration)
  }

  private fun resolveForTest(context: ExtensionContext): TestEnvironments {
    val testClass = context.requiredTestClass
    val testMethod = context.requiredTestMethod
    val store = context.getStore(NAMESPACE)
    val sdkTestConfiguration = store.get(SDK_TEST_CONFIGURATION, Configuration::class.java)
    val configurations =
      sdkTestConfiguration?.let { listOf(it) } ?: Plans.of(context, testClass, testMethod)
    if (configurations.isEmpty()) {
      throw TestAbortedException(Plans.noSdkReason(context, testClass, testMethod))
    }
    val ofClass = store.get(CLASS_ENVIRONMENTS, TestEnvironments::class.java)?.environments
    // The test shares the environments of its class that it is configured for.
    val shared = configurations.map { configuration ->
      ofClass?.firstOrNull { it.configuration == configuration }
    }
    return when {
      ofClass == null -> TestEnvironments.open(Plans.session(context), configurations)
      null !in shared -> TestEnvironments(shared.filterNotNull(), shared = true)
      // A RobolectricSdkTest asks for each of its SDKs, so it gets an environment of its own.
      sdkTestConfiguration != null -> TestEnvironments.open(Plans.session(context), configurations)
      else ->
        error(
          "${Plans.describe(testClass, testMethod)} is configured differently than the Android " +
            "environments it shares with other tests: a class with @BeforeAll or @AfterAll " +
            "methods or @TestInstance(PER_CLASS) has one environment for each of its SDKs for " +
            "all its tests, including those of its @Nested classes. Configure the test like " +
            "that class, move it to a class of its own, or annotate it with " +
            "@RobolectricSdkTest to run it in an environment of its own."
        )
    }
  }
}
