package org.robolectric.simulator.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SimulatorPluginTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test
  fun tasks_listsSimulateTasksForEachVariant() {
    val project = AndroidTestProject(tempFolder.root, appBuildScript = FLAVORS)

    val result = project.build("tasks", "--all")

    assertThat(result.output).contains("simulateDemoDebug")
    assertThat(result.output).contains("simulateProdDebug")
    assertThat(result.output).contains("simulateDemoRelease")
  }

  @Test
  fun tasks_simulatorPluginAppliedBeforeTheAndroidPlugin_listsSimulateTasks() {
    val project =
      AndroidTestProject(tempFolder.root, appBuildScript = FLAVORS, simulatorPluginFirst = true)

    val result = project.build("tasks", "--all")

    assertThat(result.output).contains("simulateDemoDebug")
  }

  @Test
  fun simulate_buildsTheTestResourcesButDoesNotRunTheTests() {
    val project = AndroidTestProject(tempFolder.root, appBuildScript = FLAVORS)

    // --dry-run builds the task graph without running anything.
    val result = project.build("simulateDemoDebug", "--dry-run")

    assertThat(result.output).contains(":app:packageDemoDebugUnitTestForUnitTest SKIPPED")
    assertThat(result.output).contains(":app:simulateDemoDebug SKIPPED")
    assertThat(result.output).doesNotContain(":app:testDemoDebugUnitTest SKIPPED")
  }

  @Test
  fun simulate_buildsTheClassesButNotTheApk() {
    val project = AndroidTestProject(tempFolder.root, appBuildScript = FLAVORS)

    val result = project.build("simulateDemoDebug", "--dry-run")

    // The simulator runs the app from the test classpath and the resources in the .ap_ file.
    assertThat(result.output).contains(":app:compileDemoDebugJavaWithJavac SKIPPED")
    assertThat(result.output).contains(":app:compileDemoDebugUnitTestJavaWithJavac SKIPPED")
    assertThat(result.output).doesNotContain(":app:packageDemoDebug SKIPPED")
    assertThat(result.output).doesNotContain(":app:assembleDemoDebug SKIPPED")
  }

  @Test
  fun simulate_usesTheSimulatorOfTheRobolectricVersionInTestImplementation() {
    val project = AndroidTestProject(tempFolder.root, appBuildScript = FLAVORS)
    // The plugin adds the simulator dependency when the simulate task is configured, so print the
    // classpath that task runs with rather than a dependency report that doesn't configure it.
    tempFolder.root
      .resolve("app/build.gradle.kts")
      .appendText(
        """

        tasks.register("printSimulateClasspath") {
          val classpath = tasks.named<JavaExec>("simulateDemoDebug").map { it.classpath }
          doLast { classpath.get().forEach { println("Simulate classpath: " + it.name) } }
        }
        """
          .trimIndent(),
      )

    val result = project.build("printSimulateClasspath")

    assertThat(result.output).contains("Simulate classpath: simulator-4.17.jar")
    assertThat(result.output).doesNotContain("No compatible Robolectric version found")
  }

  @Test
  fun simulate_withDependencyLocking_resolvesTheSimulatorOutsideTheTestClasspath() {
    val project =
      AndroidTestProject(
        tempFolder.root,
        extraBuildScript =
          """
          dependencyLocking { lockAllConfigurations() }

          tasks.register("printClasspaths") {
            val simulate = tasks.named<JavaExec>("simulateDebug").map { it.classpath }
            val unitTest = tasks.named<Test>("testDebugUnitTest").map { it.classpath }
            doLast {
              simulate.get().forEach { println("Simulate classpath: " + it.name) }
              unitTest.get().forEach { println("Unit test classpath: " + it.name) }
            }
          }
          """,
      )
    // Lock the app's own dependencies, as a project that uses locking would have.
    project.build(":app:dependencies", "--write-locks")

    val result = project.build("printClasspaths")

    assertThat(result.output).contains("Simulate classpath: simulator-4.17.jar")
    assertThat(result.output).contains("Unit test classpath: robolectric-4.17.jar")
    assertThat(result.output).doesNotContain("Unit test classpath: simulator-4.17.jar")
  }

  @Test
  fun simulate_variantWithoutUnitTests_explainsWhy() {
    val project = AndroidTestProject(tempFolder.root, appBuildScript = FLAVORS)

    // --dry-run builds the task graph without running anything.
    val result = project.buildAndFail("simulateDemoRelease", "--dry-run")

    assertThat(result.output).contains("packageDemoReleaseUnitTestForUnitTest")
    assertThat(result.output).contains("variants that have unit tests")
  }

  @Test
  fun simulate_withoutAndroidResources_explainsTheSetting() {
    val project =
      AndroidTestProject(
        tempFolder.root,
        appBuildScript = FLAVORS,
        includeAndroidResources = false,
      )

    // --dry-run builds the task graph without running anything.
    val result = project.buildAndFail("simulateDemoDebug", "--dry-run")

    assertThat(result.output).contains("packageDemoDebugUnitTestForUnitTest")
    assertThat(result.output).contains("isIncludeAndroidResources")
  }

  @Test
  fun simulate_usesTheHeapSizeOfTheUnitTests() {
    val project =
      AndroidTestProject(
        tempFolder.root,
        appBuildScript =
          """
          testOptions.unitTests.all {
            it.minHeapSize = "512m"
            it.maxHeapSize = "3g"
          }
          """,
        extraBuildScript = PRINT_SIMULATE_JVM_ARGS,
      )

    val result = project.build("printSimulateJvmArgs")

    assertThat(simulateJvmArgs(result)).containsAtLeast("-Xms512m", "-Xmx3g")
  }

  @Test
  fun simulate_respectsTheRequestedWindowSize() {
    val project = AndroidTestProject(tempFolder.root, extraBuildScript = PRINT_SIMULATE_JVM_ARGS)

    val result = project.build("printSimulateJvmArgs")

    // Without it, dialogs and other windows that don't fill the screen are drawn at the top left.
    assertThat(simulateJvmArgs(result))
      .contains("-Drobolectric.windowManager.useRequestedSize=true")
  }

  @Test
  fun simulate_bindsRealServices() {
    val project = AndroidTestProject(tempFolder.root, extraBuildScript = PRINT_SIMULATE_JVM_ARGS)

    val result = project.build("printSimulateJvmArgs")

    // Apps and libraries that bind to their own services need the binder those services return.
    assertThat(simulateJvmArgs(result)).contains("-Drobolectric.bindRealServices=true")
  }

  @Test
  fun simulate_installsTheDefaultUncaughtExceptionHandler() {
    val project = AndroidTestProject(tempFolder.root, extraBuildScript = PRINT_SIMULATE_JVM_ARGS)

    val result = project.build("printSimulateJvmArgs")

    // Crash reporters wrap the default handler, which Android always has.
    assertThat(simulateJvmArgs(result))
      .contains("-Drobolectric.installDefaultUncaughtExceptionHandler=true")
  }

  @Test
  fun simulate_isCompatibleWithTheConfigurationCache() {
    // Skip the simulator itself: the configuration cache entry, which holds the task's state, is
    // stored before any task runs, and reused by the second build.
    val project =
      AndroidTestProject(
        tempFolder.root,
        extraBuildScript =
          """tasks.matching { it.name == "simulateDebug" }.configureEach { onlyIf { false } }""",
      )

    val firstResult = project.build("simulateDebug", "--configuration-cache")
    val secondResult = project.build("simulateDebug", "--configuration-cache")

    assertThat(firstResult.output).contains("Configuration cache entry stored.")
    assertThat(secondResult.output).contains("Configuration cache entry reused.")
  }

  private fun simulateJvmArgs(result: BuildResult): List<String> =
    result.output
      .lines()
      .filter { it.startsWith(SIMULATE_JVM_ARG_PREFIX) }
      .map { it.removePrefix(SIMULATE_JVM_ARG_PREFIX) }

  private companion object {
    const val SIMULATE_JVM_ARG_PREFIX = "simulateDebug JVM arg: "

    // Prints the JVM arguments of the simulate task, which --dry-run does not show.
    const val PRINT_SIMULATE_JVM_ARGS =
      """
      tasks.register("printSimulateJvmArgs") {
        doLast {
          val jvmArgs = tasks.named<JavaExec>("simulateDebug").get().allJvmArgs
          jvmArgs.forEach { println("$SIMULATE_JVM_ARG_PREFIX" + it) }
        }
      }
      """

    const val FLAVORS =
      """
      flavorDimensions += "env"
      productFlavors {
        create("demo") { dimension = "env" }
        create("prod") { dimension = "env" }
      }
      """
  }
}
