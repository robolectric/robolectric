package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Tests for [RunnerLogger]. */
@OptIn(ExperimentalRunnerApi::class)
class RunnerLoggerTest {

  private lateinit var originalOutput: PrintStream
  private lateinit var testOutput: ByteArrayOutputStream

  @BeforeEach
  fun setUp() {
    originalOutput = RunnerLogger.output
    testOutput = ByteArrayOutputStream()
    RunnerLogger.output = PrintStream(testOutput)
  }

  @AfterEach
  fun tearDown() {
    RunnerLogger.output = originalOutput
    RunnerLogger.isDebugEnabled = false
  }

  @Test
  fun `debug messages are suppressed when debug is disabled`() {
    RunnerLogger.isDebugEnabled = false
    RunnerLogger.debug("test message")

    assertThat(testOutput.toString()).isEmpty()
  }

  @Test
  fun `debug messages are output when debug is enabled`() {
    RunnerLogger.isDebugEnabled = true
    RunnerLogger.debug("test message")

    assertThat(testOutput.toString()).contains("[DEBUG]")
    assertThat(testOutput.toString()).contains("test message")
  }

  @Test
  fun `warn messages are always output`() {
    RunnerLogger.isDebugEnabled = false
    RunnerLogger.warn("warning message")

    assertThat(testOutput.toString()).contains("[WARN]")
    assertThat(testOutput.toString()).contains("warning message")
  }

  @Test
  fun `error messages include throwable stack trace`() {
    val exception = RuntimeException("test error")
    RunnerLogger.error("error message", exception)

    assertThat(testOutput.toString()).contains("[ERROR]")
    assertThat(testOutput.toString()).contains("error message")
    assertThat(testOutput.toString()).contains("RuntimeException")
  }

  @Test
  fun `logSandboxCreation includes all parameters`() {
    RunnerLogger.isDebugEnabled = true
    RunnerLogger.logSandboxCreation(
      RunnerLogger.SandboxCreationInfo(
        testClass = "MyTest",
        testMethod = "testMethod",
        sdkApiLevel = 29,
        looperMode = "PAUSED",
        sqliteMode = "NATIVE",
        graphicsMode = "NATIVE",
      )
    )

    val output = testOutput.toString()
    assertThat(output).contains("MyTest")
    assertThat(output).contains("testMethod")
    assertThat(output).contains("SDK=29")
    assertThat(output).contains("Looper=PAUSED")
  }

  @Test
  fun `logSdkFallback outputs warning`() {
    RunnerLogger.logSdkFallback("MyTest", "testMethod", 28, "test reason")

    val output = testOutput.toString()
    assertThat(output).contains("[WARN]")
    assertThat(output).contains("SDK fallback")
    assertThat(output).contains("MyTest.testMethod")
    assertThat(output).contains("SDK 28")
  }

  @Test
  fun `lazy debug evaluation does not execute when disabled`() {
    RunnerLogger.isDebugEnabled = false
    var called = false

    RunnerLogger.debug {
      called = true
      "expensive message"
    }

    assertThat(called).isFalse()
    assertThat(testOutput.toString()).isEmpty()
  }

  @Test
  fun `lazy debug evaluation executes when enabled`() {
    RunnerLogger.isDebugEnabled = true
    var called = false

    RunnerLogger.debug {
      called = true
      "expensive message"
    }

    assertThat(called).isTrue()
    assertThat(testOutput.toString()).contains("expensive message")
  }
}
