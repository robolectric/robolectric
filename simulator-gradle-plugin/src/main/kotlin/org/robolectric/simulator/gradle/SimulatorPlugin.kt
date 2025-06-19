package org.robolectric.simulator.gradle

import com.android.build.gradle.AppPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/** A plugin to launch the Robolectric simulator. */
class SimulatorPlugin : Plugin<Project> {

  private companion object {
    // Use a constant to avoid a heavy and unnecessary dependency on Robolectric
    private const val MAIN_CLASS = "org.robolectric.simulator.SimulatorMain"
    private val REQUIRED_VERSION = intArrayOf(4, 15)
  }

  override fun apply(project: Project) {
    project.plugins.withType<AppPlugin> {
      project.tasks.register<JavaExec>("simulate") {
        group = "simulation"
        description = "Runs the Robolectric simulator"
        configureTask(project, this)
      }
    }
  }

  /** Checks if a version string is at least 4.15, when the simulator was introduced. */
  private fun supportsSimulator(currentVersion: String): Boolean {
    val currentNumericPart = currentVersion.split('-')[0]

    val currentParts = currentNumericPart.split('.').mapNotNull { it.toIntOrNull() }

    val major = currentParts.getOrElse(0) { 0 }
    val minor = currentParts.getOrElse(1) { 0 }

    return (major >= REQUIRED_VERSION[0] && minor >= REQUIRED_VERSION[1])
  }

  private fun configureTask(project: Project, task: JavaExec) {
    // Find the 'apk-for-local-test.ap_' file
    val packageTaskName = "packageDebugUnitTestForUnitTest"
    val targetTask =
      project.tasks.findByName(packageTaskName)
        ?: throw GradleException(
          "The '$packageTaskName' task was not found. " +
            "Check that you have set 'android.testOptions.unitTests.isIncludeAndroidResources = true'" +
            " in your build.gradle(.kts) file."
        )
    val resourceApkFile =
      targetTask.outputs.files.find { it.name.endsWith(".ap_") }
        ?: throw GradleException(
          "Could not find an .ap_ file in the outputs of task '$packageTaskName'."
        )
    val testTask = project.tasks.getByName<Test>("testDebugUnitTest")

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
      mainClass.set(MAIN_CLASS)
      args = listOf(resourceApkFile.absolutePath)
      dependsOn(targetTask, "assembleDebug")
      standardOutput = System.out
      errorOutput = System.err
    }
  }
}
