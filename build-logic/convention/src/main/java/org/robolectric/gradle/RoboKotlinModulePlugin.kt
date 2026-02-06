package org.robolectric.gradle

import java.io.File
import java.lang.Boolean.getBoolean
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class RoboKotlinModulePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("org.robolectric.gradle.AarDepsPlugin")
    project.pluginManager.apply("org.jetbrains.kotlin.jvm")

    project.tasks.withType<KotlinJvmCompile>().configureEach {
      // Some libraries like junit platform engine requires JDK 17.
      compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    project.tasks.withType<JavaCompile>().configureEach {
      sourceCompatibility = JavaVersion.VERSION_17.toString()
      targetCompatibility = JavaVersion.VERSION_17.toString()

      // Show all warnings except boot classpath
      if (getBoolean("lint")) {
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
      exclude("**/*$*")

      configureTestTask()
    }
  }
}
