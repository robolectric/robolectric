package org.robolectric.runner.common
/**
 * Result of a sandboxed test execution.
 *
 * This sealed class represents the three possible outcomes of test execution:
 * - [Success] - The test passed
 * - [Failure] - The test failed with an error
 * - [Skipped] - The test was skipped
 *
 * ## Usage
 *
 * ```kotlin
 * val result = executor.executeTest(testClass, testMethod) { context ->
 *   // test code
 * }
 *
 * when (result) {
 *   is ExecutionResult.Success -> println("Test passed in ${result.durationMs}ms")
 *   is ExecutionResult.Failure -> println("Test failed: ${result.error.message}")
 *   is ExecutionResult.Skipped -> println("Test skipped: ${result.reason}")
 * }
 * ```
 */
@ExperimentalRunnerApi
sealed class ExecutionResult {
  /** Whether the test passed successfully. */
  abstract val isSuccess: Boolean
  /** Duration of test execution in milliseconds (0 for skipped tests). */
  abstract val durationMs: Long

  /**
   * Test execution completed successfully.
   *
   * @property durationMs Time taken to execute the test in milliseconds
   */
  data class Success(override val durationMs: Long) : ExecutionResult() {
    override val isSuccess: Boolean = true
  }

  /**
   * Test execution failed with an error.
   *
   * @property error The throwable that caused the failure
   * @property durationMs Time taken until the failure occurred
   */
  data class Failure(val error: Throwable, override val durationMs: Long = 0) : ExecutionResult() {
    override val isSuccess: Boolean = false
    /** The error message, or "Unknown error" if null. */
    val message: String
      get() = error.message ?: "Unknown error"
  }

  /**
   * Test was skipped and not executed.
   *
   * @property reason Human-readable reason for skipping
   */
  data class Skipped(val reason: String) : ExecutionResult() {
    override val isSuccess: Boolean = false
    override val durationMs: Long = 0
  }

  companion object {
    /** Creates a successful result with the given duration. */
    @JvmStatic fun success(durationMs: Long = 0): ExecutionResult = Success(durationMs)

    /** Creates a failure result with the given error and duration. */
    @JvmStatic
    fun failure(error: Throwable, durationMs: Long = 0): ExecutionResult =
      Failure(error, durationMs)

    /** Creates a skipped result with the given reason. */
    @JvmStatic fun skipped(reason: String): ExecutionResult = Skipped(reason)
  }
}
