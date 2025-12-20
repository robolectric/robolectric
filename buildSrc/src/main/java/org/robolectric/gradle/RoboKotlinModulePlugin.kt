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
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class RoboKotlinModulePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("org.robolectric.gradle.AarDepsPlugin")

    project.tasks.withType<JavaCompile>().configureEach {
      sourceCompatibility = JavaVersion.VERSION_21.toString()
      targetCompatibility = JavaVersion.VERSION_21.toString()

      // Show all warnings except boot classpath
      if (System.getProperty("lint") != null && "false" != System.getProperty("lint")) {
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

      configureTestTask()
    }
  }

  private val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
}
