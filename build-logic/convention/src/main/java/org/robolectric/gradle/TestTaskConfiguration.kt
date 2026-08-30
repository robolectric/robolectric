package org.robolectric.gradle

import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.process.CommandLineArgumentProvider

fun Test.configureTestTask() {
  testLogging {
    exceptionFormat = TestExceptionFormat.FULL
    showCauses = true
    showExceptions = true
    showStackTraces = true
    showStandardStreams = true
    events =
      setOf(
        TestLogEvent.FAILED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STANDARD_OUT,
        TestLogEvent.STANDARD_ERROR,
      )
  }

  minHeapSize = "2g"
  maxHeapSize = "12g"

  System.getenv("GRADLE_MAX_PARALLEL_FORKS")?.toIntOrNull()?.let { maxParallelForks = it }

  jvmArgumentProviders.add(
    RobolectricJvmArgumentsProvider(
      robolectricPropertiesProvider = project.providers.systemPropertiesPrefixedBy("robolectric."),
      logger = logger,
    )
  )
  jvmArgumentProviders.add(DefaultJvmArgumentsProvider())
}

private class RobolectricJvmArgumentsProvider(
  @get:Input private val robolectricPropertiesProvider: Provider<Map<String, String>>,
  @get:Internal private val logger: Logger,
) : CommandLineArgumentProvider {
  override fun asArguments(): Iterable<String> {
    val robolectricProperties =
      robolectricPropertiesProvider.getOrElse(emptyMap()).map { (key, value) -> "-D$key=$value" }

    if (robolectricProperties.isNotEmpty()) {
      logger.lifecycle("Running tests with $robolectricProperties")
    }

    return robolectricProperties
  }
}

private class DefaultJvmArgumentsProvider : CommandLineArgumentProvider {
  override fun asArguments(): Iterable<String> {
    return listOf(
      // The native runtime is loaded with System.load from the classpath, which JDK 24 made a
      // restricted method (JEP 472). Without this every run prints a warning, and once the JDK
      // switches its default to deny it will throw IllegalCallerException instead. Older JDKs
      // accept the flag and ignore it.
      "--enable-native-access=ALL-UNNAMED",
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
      "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    )
  }
}
