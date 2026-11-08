package org.robolectric.simulator.gradle

import com.google.common.truth.Truth.assertThat
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

  private companion object {
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
