package org.robolectric.simulator.gradle

import com.android.build.gradle.AppExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByName
import org.robolectric.simulator.SimulatorMain

/** A plugin to launch the Robolectric simulator. */
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

  /** Checks if a version string is at least 4.15, when the simulator was introduced. */
  private fun supportsSimulator(currentVersion: String): Boolean {
    val currentNumericPart = currentVersion.split('-')[0]

    val currentParts = currentNumericPart.split('.').mapNotNull { it.toIntOrNull() }

    val major = currentParts.getOrElse(0) { 0 }
    val minor = currentParts.getOrElse(1) { 0 }

    return (major >= 4 && minor >= 15)
  }

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

    val robolectricDependencies =
      project.configurations.getByName("testImplementation").allDependencies.filter {
        it.group == "org.robolectric"
      }
    check(robolectricDependencies.isNotEmpty()) {
      "Missing Robolectric dependency in the 'testImplementation' configuration."
    }

    if (robolectricDependencies.none { it.name == "simulator" }) {
      val robolectricVersion =
        robolectricDependencies.maxBy { it.version.orEmpty() }.version.orEmpty()

      check(supportsSimulator(robolectricVersion)) {
        "Robolectric 4.15 or above is required for the simulator"
      }

      project.dependencies.add(
        "testImplementation",
        "org.robolectric:simulator:$robolectricVersion",
      )
    }

    val robolectricJvmArgs =
      listOf(
        "-Drobolectric.logging.enabled=true",
        "-Drobolectric.logging=stdout",
        "-Drobolectric.createActivityContexts=true",
        "-Drobolectric.useEmbeddedViewRoot=true",
      )

    task.apply {
      classpath = testTask.classpath
      jvmArgs = testTask.jvmArgs + robolectricJvmArgs
      mainClass.set(SimulatorMain::class.qualifiedName)
      args = listOf(resourceApkFile.absolutePath)
      dependsOn(testTaskName, "assembleDebug")
      standardOutput = System.out
      errorOutput = System.err
    }
  }
}
