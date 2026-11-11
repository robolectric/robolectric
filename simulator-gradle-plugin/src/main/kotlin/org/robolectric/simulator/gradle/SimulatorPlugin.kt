package org.robolectric.simulator.gradle

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import java.util.concurrent.Callable
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.process.CommandLineArgumentProvider

/** A plugin to launch the Robolectric simulator. */
class SimulatorPlugin : Plugin<Project> {

  private companion object {
    // Use a constant to avoid a heavy and unnecessary dependency on Robolectric
    private const val MAIN_CLASS = "org.robolectric.simulator.SimulatorMain"
    private val REQUIRED_VERSION = Version("4.15")
  }

  override fun apply(project: Project) {
    // The Android plugin may be applied after this one, so wait for it to add its extension.
    project.pluginManager.withPlugin("com.android.application") {
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
  }

  private fun configureTask(project: Project, task: JavaExec, variantName: String) {
    // The Android Gradle plugin registers these tasks after the variant callbacks run, so look
    // them up only when this task is about to run. Failing earlier would break every build of the
    // project, including './gradlew tasks'.
    val packageTaskName = "package${variantName}UnitTestForUnitTest"
    val packageTask = project.provider {
      project.tasks.findByName(packageTaskName)
        ?: throw GradleException(
          "The simulator needs the '$packageTaskName' task. The Android Gradle plugin only " +
            "creates it for variants that have unit tests, by default the debug ones, and " +
            "only when 'android.testOptions.unitTests.isIncludeAndroidResources' is true.",
        )
    }
    val testTask = project.provider { project.tasks.getByName<Test>("test${variantName}UnitTest") }

    // Variants without unit tests don't have these configurations. Apps usually declare
    // Robolectric in 'testImplementation', which 'test<Variant>Implementation' doesn't extend, so
    // look for it in the unit test runtime classpath, which extends all the variant's test
    // configurations.
    val unitTestRuntimeClasspath =
      "${variantName.replaceFirstChar { it.lowercase() }}UnitTestRuntimeClasspath"
    project.configurations
      .matching { it.name == "test${variantName}Implementation" }
      .configureEach {
        val robolectricDependencies =
          project.configurations.getByName(unitTestRuntimeClasspath).allDependencies.filter {
            it.group == "org.robolectric"
          }

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
        "-Drobolectric.windowManager.useRequestedSize=true",
      )

    task.apply {
      // The test task's classpath carries the tasks that build it, but not the test task itself.
      classpath = project.files(Callable { testTask.get().classpath })
      jvmArgumentProviders.add(
        CommandLineArgumentProvider {
          val test = testTask.get()
          // Gradle keeps the heap sizes apart from the other JVM arguments.
          val heapSizeArgs =
            listOfNotNull(test.minHeapSize?.let { "-Xms$it" }, test.maxHeapSize?.let { "-Xmx$it" })
          test.jvmArgs + heapSizeArgs + robolectricJvmArgs
        },
      )
      mainClass.set(MAIN_CLASS)
      // The 'apk-for-local-test.ap_' file.
      argumentProviders.add(
        CommandLineArgumentProvider {
          val resourceApkFile =
            packageTask.get().outputs.files.find { it.name.endsWith(".ap_") }
              ?: throw GradleException(
                "Could not find an .ap_ file in the outputs of task '$packageTaskName'.",
              )
          listOf(resourceApkFile.absolutePath)
        },
      )
      dependsOn(packageTask)
      standardOutput = System.out
      errorOutput = System.err
    }
  }
}
