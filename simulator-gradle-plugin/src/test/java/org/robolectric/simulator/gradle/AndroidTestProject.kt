package org.robolectric.simulator.gradle

import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * A minimal Android app project in [dir] that applies the simulator plugin, for Gradle TestKit
 * tests. [appBuildScript] holds extra configuration for the app module's `android` block, such as
 * product flavors.
 */
internal class AndroidTestProject(
  private val dir: File,
  appBuildScript: String = "",
  includeAndroidResources: Boolean = true,
) {
  init {
    dir
      .resolve("settings.gradle.kts")
      .writeText(
        """
        pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
        dependencyResolutionManagement { repositories { google(); mavenCentral() } }
        rootProject.name = "simulator-test-project"
        include(":app")
        """
          .trimIndent(),
      )
    androidSdkDir()?.let { dir.resolve("local.properties").writeText("sdk.dir=$it\n") }
    dir.resolve("app").mkdirs()
    dir
      .resolve("app/build.gradle.kts")
      .writeText(
        """
      plugins {
        id("com.android.application")
        id("org.robolectric.simulator")
      }

      android {
        namespace = "org.robolectric.simulator.testproject"
        compileSdk = 36
        defaultConfig { minSdk = 23 }
        testOptions { unitTests.isIncludeAndroidResources = $includeAndroidResources }
      $appBuildScript
      }

      dependencies { testImplementation("org.robolectric:robolectric:4.17") }
      """
          .trimIndent(),
      )
    dir.resolve("app/src/main").mkdirs()
    dir.resolve("app/src/main/AndroidManifest.xml").writeText("<manifest />\n")
  }

  /** Runs Gradle with [arguments], expecting the build to succeed. */
  fun build(vararg arguments: String): BuildResult = runner(*arguments).build()

  /** Runs Gradle with [arguments], expecting the build to fail. */
  fun buildAndFail(vararg arguments: String): BuildResult = runner(*arguments).buildAndFail()

  private fun runner(vararg arguments: String): GradleRunner =
    GradleRunner.create()
      .withProjectDir(dir)
      .withPluginClasspath()
      .withArguments(*arguments, "--stacktrace")
      .forwardOutput()

  private fun androidSdkDir(): String? =
    System.getProperty("android.sdk.dir") ?: System.getenv("ANDROID_HOME")
}
