package org.robolectric.runner.common

import java.io.PrintStream
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Configurable logger for Robolectric runner infrastructure.
 *
 * This logger provides debug-level logging for sandbox lifecycle events, helping developers
 * diagnose issues with sandbox creation, reuse, and teardown.
 *
 * ## Usage
 *
 * Enable via system property:
 * ```
 * -Drobolectric.runner.debug=true
 * ```
 *
 * Or programmatically:
 * ```kotlin
 * RunnerLogger.isDebugEnabled = true
 * ```
 *
 * ## Log Levels
 * - DEBUG: Detailed information about sandbox lifecycle decisions
 * - INFO: High-level events (sandbox created, test started)
 * - WARN: Potential issues (fallback SDK used, configuration mismatch)
 * - ERROR: Failures that may affect test execution
 */
@Suppress("TooManyFunctions")
@ExperimentalRunnerApi
object RunnerLogger {

  /** System property to enable debug logging. */
  const val DEBUG_PROPERTY = "robolectric.runner.debug"

  /** System property to set log level (DEBUG, INFO, WARN, ERROR). */
  const val LOG_LEVEL_PROPERTY = "robolectric.runner.logLevel"

  /** Log levels in order of verbosity. */
  enum class Level {
    DEBUG,
    INFO,
    WARN,
    ERROR,
  }

  /** Current log level. */
  var logLevel: Level = Level.INFO
    private set

  /** Whether debug logging is enabled. */
  var isDebugEnabled: Boolean = false
    set(value) {
      field = value
      if (value && logLevel.ordinal > Level.DEBUG.ordinal) {
        logLevel = Level.DEBUG
      }
    }

  /** Output stream for log messages. Defaults to System.err. */
  var output: PrintStream = System.err

  /** Prefix for all log messages. */
  private const val PREFIX = "[Robolectric]"

  /** Date formatter for timestamps. */
  private val timeFormatter = DateTimeFormatter.ISO_INSTANT

  init {
    // Initialize from system properties
    isDebugEnabled = System.getProperty(DEBUG_PROPERTY, "false").toBoolean()
    System.getProperty(LOG_LEVEL_PROPERTY)?.let { levelStr ->
      try {
        logLevel = Level.valueOf(levelStr.uppercase())
      } catch (_: IllegalArgumentException) {
        // Ignore invalid level
      }
    }
  }

  /** Logs a debug message. Only output if debug is enabled. */
  fun debug(message: String) {
    if (isDebugEnabled) {
      log(Level.DEBUG, message)
    }
  }

  /** Logs a debug message with lazy evaluation. */
  inline fun debug(messageProvider: () -> String) {
    if (isDebugEnabled) {
      log(Level.DEBUG, messageProvider())
    }
  }

  /** Logs an info message. */
  fun info(message: String) {
    if (logLevel.ordinal <= Level.INFO.ordinal) {
      log(Level.INFO, message)
    }
  }

  /** Logs a warning message. */
  fun warn(message: String) {
    if (logLevel.ordinal <= Level.WARN.ordinal) {
      log(Level.WARN, message)
    }
  }

  /** Logs an error message. */
  fun error(message: String, throwable: Throwable? = null) {
    if (logLevel.ordinal <= Level.ERROR.ordinal) {
      log(Level.ERROR, message)
      throwable?.printStackTrace(output)
    }
  }

  /** Internal log method. Made public for inline function access. */
  @PublishedApi
  internal fun log(level: Level, message: String) {
    val timestamp = timeFormatter.format(Instant.now())
    val threadName = Thread.currentThread().name
    output.println("$timestamp $PREFIX [$level] [$threadName] $message")
  }

  // ========== Sandbox Lifecycle Logging ==========

  /** Data class for sandbox creation details. */
  data class SandboxCreationInfo(
    val testClass: String,
    val testMethod: String?,
    val sdkApiLevel: Int,
    val looperMode: String,
    val sqliteMode: String,
    val graphicsMode: String,
  )

  /** Log sandbox creation. */
  fun logSandboxCreation(info: SandboxCreationInfo) {
    debug {
      buildString {
        append("Creating sandbox for ${info.testClass}")
        if (info.testMethod != null) append(".${info.testMethod}")
        append(" [SDK=${info.sdkApiLevel}, Looper=${info.looperMode}, ")
        append("SQLite=${info.sqliteMode}, Graphics=${info.graphicsMode}]")
      }
    }
  }

  /** Log SDK selection. */
  fun logSdkSelection(testClass: String, selectedSdks: List<Int>, configuredSdks: List<Int>) {
    debug { "SDK selection for $testClass: selected=$selectedSdks, configured=$configuredSdks" }
  }

  /** Log SDK fallback. */
  fun logSdkFallback(testClass: String, testMethod: String, fallbackSdk: Int, reason: String) {
    warn("SDK fallback for $testClass.$testMethod: using SDK $fallbackSdk ($reason)")
  }

  /** Log sandbox reuse. */
  fun logSandboxReuse(testClass: String, sdkApiLevel: Int) {
    debug { "Reusing sandbox for $testClass [SDK=$sdkApiLevel]" }
  }

  /** Log sandbox teardown. */
  fun logSandboxTeardown(testClass: String) {
    debug { "Tearing down sandbox for $testClass" }
  }

  // ========== Class Lifecycle Logging ==========

  /** Log class context creation. */
  fun logClassContextCreated(testClass: String, sdkApiLevel: Int) {
    debug { "Class context created for $testClass [SDK=$sdkApiLevel]" }
  }

  /** Log class context reuse. */
  fun logClassContextReused(testClass: String) {
    debug { "Class context reused for $testClass" }
  }

  /** Log class context teardown. */
  fun logClassContextTeardown(testClass: String) {
    debug { "Class context torn down for $testClass" }
  }

  // ========== Test Execution Logging ==========

  /** Log test start. */
  fun logTestStart(testClass: String, testMethod: String, sdkApiLevel: Int) {
    debug { "Test started: $testClass.$testMethod [SDK=$sdkApiLevel]" }
  }

  /** Log test end. */
  fun logTestEnd(testClass: String, testMethod: String, durationMs: Long, success: Boolean) {
    debug {
      val status = if (success) "PASSED" else "FAILED"
      "Test ended: $testClass.$testMethod [$status, ${durationMs}ms]"
    }
  }
}
