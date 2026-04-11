package org.robolectric.gradle

import com.android.SdkConstants.FD_GENERATED
import com.android.build.api.dsl.LibraryExtension
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

class AndroidProjectConfigPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("com.android.library")

    project.tasks.withType<Test>().configureEach { configureTestTask() }

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
