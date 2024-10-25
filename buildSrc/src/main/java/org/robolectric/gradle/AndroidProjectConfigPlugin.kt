package org.robolectric.gradle

import ProvideBuildClasspathTask
import com.android.SdkConstants.FD_GENERATED
import com.android.build.api.dsl.LibraryExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class AndroidProjectConfigPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("com.android.library")

    project.tasks.withType<Test>().configureEach {
      // TODO: DRY up code with RoboJavaModulePlugin...
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
      val defaultJvmArgs =
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

      jvmArgs = systemJvmArgs + defaultJvmArgs

      doFirst {
        if (systemJvmArgs.isNotEmpty()) {
          println("Running tests with $systemJvmArgs")
        }
      }
    }

    project.tasks.register<ProvideBuildClasspathTask>("provideBuildClasspath") {
      val outDir = project.layout.buildDirectory.dir("$FD_GENERATED/robolectric").get().asFile

      outFile = File(outDir, "robolectric-deps.properties")

      project.extensions.configure<LibraryExtension> {
        sourceSets.getByName("test").resources.srcDir(outDir)
      }
    }

    project.afterEvaluate {
      project.tasks.forEach { task ->
        if (task.name.matches(UNIT_TEST_JAVA_RES_TASK_NAME_REGEX)) {
          task.dependsOn("provideBuildClasspath")
        }
      }
    }

    // Only run tests in the debug variant. This is to avoid running tests twice when
    // `./gradlew test` is run at the top-level.
    project.tasks.withType<Test>().configureEach { onlyIf { name.lowercase().contains("debug") } }
  }

  private companion object {
    private val UNIT_TEST_JAVA_RES_TASK_NAME_REGEX = "process.*UnitTestJavaRes".toRegex()
  }
}
