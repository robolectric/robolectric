package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

/** Integration tests for [RobolectricSandboxExecutor]. */
@ExperimentalRunnerApi
class RobolectricSandboxExecutorIntegrationTest {
  private lateinit var deps: RobolectricDependencies
  private lateinit var lifecycleManager: SandboxLifecycleManager
  private lateinit var executor: RobolectricSandboxExecutor

  @Before
  fun setUp() {
    deps = RobolectricDependencies.create()
    lifecycleManager = SandboxLifecycleManager(deps)
    executor = RobolectricSandboxExecutor(lifecycleManager)
  }

  @After
  fun tearDown() {
    /* Cleanup handled by lifecycle manager */
  }

  @Test
  fun `executeSandboxedSafe returns success on passing test`() {
    val testClass = SimpleTestClass::class.java
    val testMethod = testClass.getMethod("passingTest")
    val result =
      executor.executeSandboxedSafe(testClass, testMethod) { sandbox ->
        assertThat(sandbox).isNotNull()
      }
    assertThat(result.isSuccess).isTrue()
    assertThat(result.durationMs).isAtLeast(0)
  }

  @Test
  fun `executeSandboxedSafe returns failure on failing test`() {
    val testClass = SimpleTestClass::class.java
    val testMethod = testClass.getMethod("passingTest")
    val result =
      executor.executeSandboxedSafe(testClass, testMethod) { _ ->
        throw AssertionError("Test failure")
      }
    assertThat(result.isSuccess).isFalse()
    assertThat(result).isInstanceOf(ExecutionResult.Failure::class.java)
    assertThat((result as ExecutionResult.Failure).message).contains("Test failure")
  }

  @Test
  fun `executeSandboxed throws on test failure`() {
    val testClass = SimpleTestClass::class.java
    val testMethod = testClass.getMethod("passingTest")
    try {
      executor.executeSandboxed(testClass, testMethod) { _ ->
        throw IllegalStateException("Expected failure")
      }
      assertThat(false).isTrue()
    } catch (e: IllegalStateException) {
      assertThat(e.message).isEqualTo("Expected failure")
    }
  }

  @Test
  fun `multiple tests can run with same executor`() {
    val testClass = SimpleTestClass::class.java
    val method1 = testClass.getMethod("passingTest")
    val method2 = testClass.getMethod("anotherPassingTest")
    val result1 = executor.executeSandboxedSafe(testClass, method1) { _ -> }
    val result2 = executor.executeSandboxedSafe(testClass, method2) { _ -> }
    assertThat(result1.isSuccess).isTrue()
    assertThat(result2.isSuccess).isTrue()
  }

  @Config(sdk = [29])
  @Suppress("EmptyFunctionBlock")
  class SimpleTestClass {
    fun passingTest() {
      /* Empty */
    }

    fun anotherPassingTest() {
      /* Empty */
    }
  }
}
