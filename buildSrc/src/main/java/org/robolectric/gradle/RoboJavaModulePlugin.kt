package org.robolectric.gradle

import ProvideBuildClasspathTask
import java.io.File
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class RoboJavaModulePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("java-library")
    project.pluginManager.apply("org.robolectric.gradle.AarDepsPlugin")

    val skipErrorProne = System.getenv("SKIP_ERRORPRONE") == "true"
    if (!skipErrorProne) {
      project.pluginManager.apply("net.ltgt.errorprone")
      project.dependencies.add("errorprone", project.libs.findLibrary("error-prone-core").get())
      project.dependencies.add(
        "errorproneJavac",
        project.libs.findLibrary("error-prone-javac").get(),
      )
    }

    project.tasks.withType<JavaCompile>().configureEach {
      sourceCompatibility = JavaVersion.VERSION_1_8.toString()
      targetCompatibility = JavaVersion.VERSION_1_8.toString()

      // Show all warnings except boot classpath
      if (System.getProperty("lint") != null && System.getProperty("lint") != "false") {
        options.compilerArgs.add("-Xlint:all") // Turn on all warnings
      }

      options.compilerArgs.add("-Xlint:-options") // Turn off "missing" bootclasspath warning
      options.encoding = "utf-8" // Make sure source encoding is UTF-8
    }

    val provideBuildClasspath =
      project.tasks.register<ProvideBuildClasspathTask>("provideBuildClasspath") {
        project.extensions.configure<JavaPluginExtension> {
          val outDir = sourceSets.getByName("test").output.resourcesDir

          outFile = File(outDir, "robolectric-deps.properties")
        }
      }

    project.tasks.withType<Test>().configureEach {
      dependsOn(provideBuildClasspath)

      // Otherwise Gradle runs static inner classes like TestRunnerSequenceTest$SimpleTest
      exclude("**/*\$*")

      // TODO: DRY up code with AndroidProjectConfigPlugin...
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
  }

  private val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
}
