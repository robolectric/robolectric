package org.robolectric.simulator.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register

/** A plugin to launch the Robolectric simulator. */
class SimulatorPlugin : Plugin<Project> {

  private companion object {
    // Use a constant to avoid a heavy and unnecessary dependency on Robolectric
    private const val MAIN_CLASS = "org.robolectric.simulator.SimulatorMain"
    private val REQUIRED_VERSION = Version("4.15")
  }

  override fun apply(project: Project) {
    project.extensions.configure<ApplicationAndroidComponentsExtension> {
      onVariants { variant ->
        val variantName = variant.name.replaceFirstChar { it.uppercase() }

        project.tasks.register<JavaExec>("simulate$variantName") {
          group = "simulation"
          description = "Runs the Robolectric simulator for the $variantName variant"
          configureTask(project, this, variantName)
        }
      }
    }
  }

  private fun configureTask(project: Project, task: JavaExec, variantName: String) {
    // Find the 'apk-for-local-test.ap_' file
    val packageTaskName = "package${variantName}UnitTestForUnitTest"
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
    val testTask = project.tasks.getByName<Test>("test${variantName}UnitTest")

    project.configurations.named("test${variantName}Implementation").configure {
      val robolectricDependencies = allDependencies.filter { it.group == "org.robolectric" }

      if (robolectricDependencies.none { it.name == "simulator" }) {
        val robolectricVersion =
          robolectricDependencies
            .maxOfOrNull { Version(it.version.orEmpty()) }
            ?.takeIf { it >= REQUIRED_VERSION }
        if (robolectricVersion == null) {
          project.logger.warn("No compatible Robolectric version found. Using $REQUIRED_VERSION.")
        }

        val simulatorDependency =
          "org.robolectric:simulator:${robolectricVersion ?: REQUIRED_VERSION}"

        dependencies.add(project.dependencies.create(simulatorDependency))
      }
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
      dependsOn(targetTask, "assemble$variantName")
      standardOutput = System.out
      errorOutput = System.err
    }
  }
}

private class Version(val version: String) : Comparable<Version> {
  private val major: Int
  private val minor: Int
  private val patch: Int
  private val tag: String

  init {
    val versionAndTag = version.split('-', limit = 2)
    val parts = versionAndTag[0].split('.').map { it.toIntOrNull() ?: 0 }

    this.major = parts.getOrElse(0) { 0 }
    this.minor = parts.getOrElse(1) { 0 }
    this.patch = parts.getOrElse(2) { 0 }
    this.tag = versionAndTag.getOrElse(1) { "" }
  }

  override fun compareTo(other: Version): Int {
    val result = compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

    return when {
      result != 0 -> result
      tag.isNotEmpty() && other.tag.isEmpty() -> -1
      tag.isEmpty() && other.tag.isNotEmpty() -> 1
      else -> tag.compareTo(other.tag, ignoreCase = true)
    }
  }

  override fun toString(): String {
    return version
  }
}
