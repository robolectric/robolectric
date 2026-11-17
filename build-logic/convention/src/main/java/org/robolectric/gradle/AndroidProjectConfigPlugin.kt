package org.robolectric.gradle

import com.android.build.api.variant.HostTestBuilder
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class AndroidProjectConfigPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("com.android.library")

    project.tasks.withType<Test>().configureEach { configureTestTask() }

    val provideBuildClasspath =
      project.tasks.register<ProvideAndroidBuildClasspathTask>("provideBuildClasspath") {
        outFile.set(outputDir.file("robolectric-deps.properties"))
      }

    ProvideBuildClasspathTask.configure(project, provideBuildClasspath)

    // Puts robolectric-deps.properties in the unit tests' Java resources, so Robolectric loads the
    // SDK jars that Gradle resolved instead of downloading them. AGP also makes the tasks that
    // package those resources depend on provideBuildClasspath.
    project.extensions.configure<LibraryAndroidComponentsExtension> {
      onVariants { variant ->
        variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]
          ?.sources
          ?.resources
          ?.addGeneratedSourceDirectory(
            provideBuildClasspath,
            ProvideAndroidBuildClasspathTask::outputDir,
          )
      }
    }

    // Only run tests in the debug variant. This is to avoid running tests twice when
    // `./gradlew test` is run at the top-level.
    project.tasks.withType<Test>().configureEach { onlyIf { name.lowercase().contains("debug") } }
  }

  /** Writes robolectric-deps.properties into a directory, which AGP's generated sources need. */
  abstract class ProvideAndroidBuildClasspathTask : ProvideBuildClasspathTask() {
    @get:OutputDirectory abstract val outputDir: DirectoryProperty
  }
}
