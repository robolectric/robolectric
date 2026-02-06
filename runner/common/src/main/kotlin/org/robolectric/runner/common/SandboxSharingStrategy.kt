package org.robolectric.runner.common

/**
 * Strategy for sharing sandboxes across tests.
 *
 * This enum defines how Robolectric sandboxes are reused between tests, trading off between
 * isolation and performance.
 */
@ExperimentalRunnerApi
enum class SandboxSharingStrategy {
  /**
   * Create a new sandbox for each test method.
   *
   * Pros:
   * - Maximum isolation between tests
   * - Each test gets a fresh Android environment
   *
   * Cons:
   * - Slowest option due to sandbox creation overhead
   * - Higher memory usage
   */
  PER_TEST,

  /**
   * Share sandbox within a test class.
   *
   * This is the default and recommended strategy. All tests in a class share the same sandbox, with
   * the environment reset between tests.
   *
   * Pros:
   * - Good balance of isolation and performance
   * - Supports @BeforeAll/@AfterAll with shared state
   * - Significantly faster than PER_TEST
   *
   * Cons:
   * - Tests in the same class must use the same SDK
   * - Potential for state leakage if reset is incomplete
   */
  PER_CLASS,

  /**
   * Share sandbox across classes with the same SDK configuration.
   *
   * Multiple test classes using the same SDK share a sandbox.
   *
   * Pros:
   * - Best performance for large test suites
   * - Minimal sandbox creation overhead
   *
   * Cons:
   * - Higher risk of state leakage
   * - @BeforeAll/@AfterAll may not work as expected
   * - Requires careful test design
   */
  PER_SDK,

  /**
   * Use a single sandbox for all tests.
   *
   * Not recommended for most use cases. Only use if all tests use the same SDK and you have strict
   * control over test state.
   *
   * Pros:
   * - Fastest possible execution
   * - Minimal memory usage
   *
   * Cons:
   * - All tests must use the same SDK
   * - High risk of state leakage
   * - Test order may affect results
   */
  GLOBAL,
}
