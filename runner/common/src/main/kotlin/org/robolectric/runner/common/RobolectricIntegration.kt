package org.robolectric.runner.common

import java.lang.reflect.Method

/**
 * Standard interface for Robolectric framework integrations.
 *
 * Framework adapters implement this interface to participate in Robolectric's sandbox lifecycle.
 * The interface defines the core lifecycle callbacks that any test framework needs to integrate
 * with Robolectric.
 *
 * ## Lifecycle Order
 *
 * For each test class:
 * 1. [beforeClass] - Called once before any tests run
 * 2. For each test method:
 *     - [beforeTest] - Called before the test
 *     - Test execution (framework-specific)
 *     - [afterTest] - Called after the test
 * 3. [afterClass] - Called once after all tests complete
 *
 * ## Implementation Notes
 * - All lifecycle methods are called on the same thread
 * - Sandbox state is managed internally
 * - Implementations should be stateless or thread-safe
 *
 * ## Example Implementation
 *
 * ```kotlin
 * class MyFrameworkIntegration : RobolectricIntegration {
 *   private val integration = RobolectricIntegrationBuilder()
 *     .sandboxSharing(SandboxSharingStrategy.PER_CLASS)
 *     .build()
 *
 *   override fun beforeClass(testClass: Class<*>) {
 *     integration.beforeClass(testClass)
 *   }
 *
 *   override fun beforeTest(testClass: Class<*>, testMethod: Method) {
 *     integration.beforeTest(testClass, testMethod)
 *   }
 *
 *   // ... other callbacks
 * }
 * ```
 *
 * @see RobolectricIntegrationBuilder
 * @see DefaultRobolectricIntegration
 */
@ExperimentalRunnerApi
interface RobolectricIntegration {

  /**
   * Called once per test class before any tests run.
   *
   * Use this callback for class-level sandbox setup. This is where the sandbox is typically created
   * for [SandboxSharingStrategy.PER_CLASS].
   *
   * @param testClass The test class about to be executed
   */
  fun beforeClass(testClass: Class<*>)

  /**
   * Called before each test method.
   *
   * Use this callback for per-test environment setup. For [SandboxSharingStrategy.PER_TEST], the
   * sandbox is created here.
   *
   * @param testClass The test class being executed
   * @param testMethod The test method about to be executed
   */
  fun beforeTest(testClass: Class<*>, testMethod: Method)

  /**
   * Called after each test method.
   *
   * Use this callback for per-test cleanup. The environment is typically reset here to ensure test
   * isolation.
   *
   * @param testClass The test class being executed
   * @param testMethod The test method that was executed
   * @param success true if the test passed, false if it failed
   */
  fun afterTest(testClass: Class<*>, testMethod: Method, success: Boolean)

  /**
   * Called once per test class after all tests complete.
   *
   * Use this callback for class-level cleanup. The sandbox is typically torn down here for
   * [SandboxSharingStrategy.PER_CLASS].
   *
   * @param testClass The test class that was executed
   */
  fun afterClass(testClass: Class<*>)

  /**
   * Gets the current test execution context, if available.
   *
   * This is useful for framework integrations that need access to the sandbox or configuration
   * during test execution.
   *
   * @param testClass The test class
   * @return The current TestExecutionContext, or null if not in a test
   */
  fun getContext(testClass: Class<*>): TestExecutionContext?

  /**
   * Executes a block of code within the sandbox context.
   *
   * This method handles:
   * - Running on the sandbox main thread
   * - Environment setup/teardown
   * - Error handling
   *
   * @param testClass The test class
   * @param testMethod The test method
   * @param block The code to execute in the sandbox
   * @return The result of execution
   */
  fun <T> executeInSandbox(
    testClass: Class<*>,
    testMethod: Method,
    block: (TestExecutionContext) -> T,
  ): T
}
