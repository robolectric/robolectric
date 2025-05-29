package org.robolectric.simulator.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByName
import org.robolectric.simulator.SimulatorMain
import org.robolectric.simulator.gradle.generated.Version

/**
 * The plugin to use Robolectric's simulator.
 *
 * @see Plugin
 */
class SimulatorPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val androidExtension = getAndroidExtension(project)
    if (androidExtension == null) {
      return
    }

    project.afterEvaluate {
      project.tasks.register("simulate", JavaExec::class.java) {
        group = "simulation"
        description = "Runs the Robolectric simulator"
        configureTask(project, this)
      }
    }
  }

  private fun getAndroidExtension(project: Project) =
    project.extensions.findByType(AppExtension::class.java)

  private fun configureTask(project: Project, task: JavaExec) {
    // Find the 'apk-for-local-test.ap_' file
    val targetTask = project.tasks.getByName("packageDebugUnitTestForUnitTest")
    val resourceApkFile =
      targetTask.outputs.files.find { it.name.endsWith(".ap_") }
        ?: throw GradleException(
          "Could not find an .ap_ file in the outputs of task '${targetTask.name}'. "
        )
    val testTaskName = "testDebugUnitTest"
    val testTask = project.tasks.getByName<Test>(testTaskName)

    val simulator =
      project.configurations
        .detachedConfiguration(
          project.dependencies.create("org.robolectric:simulator:${Version.VERSION}")
        )
        .resolve()

    task.apply {
      classpath = testTask.classpath + project.files(simulator)
      jvmArgs = testTask.jvmArgs
      mainClass.set(SimulatorMain::class.qualifiedName)
      args = listOf(resourceApkFile.absolutePath)
      dependsOn(testTaskName, "assembleDebug")
      standardOutput = System.out
      errorOutput = System.err
    }
  }
}
