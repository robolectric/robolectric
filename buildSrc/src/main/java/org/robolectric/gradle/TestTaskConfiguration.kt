package org.robolectric.gradle

import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

private val DEFAULT_JVM_ARGS =
  listOf(
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    "--add-opens=java.base/java.io=ALL-UNNAMED",
    "--add-opens=java.base/java.net=ALL-UNNAMED",
    "--add-opens=java.base/java.nio=ALL-UNNAMED", // required for ShadowVMRuntime
    "--add-opens=java.base/java.security=ALL-UNNAMED",
    "--add-opens=java.base/java.text=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED",
    "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
    "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
  )

fun Test.configureTestTask() {
  testLogging {
    exceptionFormat = TestExceptionFormat.FULL
    showCauses = true
    showExceptions = true
    showStackTraces = true
    showStandardStreams = true
    events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
  }

  minHeapSize = "2g"
  maxHeapSize = "12g"

  System.getenv("GRADLE_MAX_PARALLEL_FORKS")?.toIntOrNull()?.let { maxParallelForks = it }

  val systemJvmArgs =
    System.getProperties()
      .filterKeys { it.toString().startsWith("robolectric.") }
      .map { (key, value) -> "-D$key=$value" }

  jvmArgs = systemJvmArgs + DEFAULT_JVM_ARGS

  doFirst {
    if (systemJvmArgs.isNotEmpty()) {
      println("Running tests with $systemJvmArgs")
    }
  }
}
