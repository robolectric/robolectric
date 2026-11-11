package org.robolectric.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice.PageAlignment.DEFAULT_FOR_SDK_VERSION
import com.android.build.api.dsl.ManagedVirtualDevice.PageAlignment.FORCE_4KB_PAGES
import com.android.utils.CpuArchitecture
import com.android.utils.osArchitecture
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get

class GradleManagedDevicePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val androidExtension = project.extensions.findByType(CommonExtension::class)
    if (androidExtension == null) {
      project.logger.warn(
        "Not applying the '{}' plugin on project '{}' because it is not an Android project",
        this::class.simpleName,
        project.path,
      )
      return
    }

    androidExtension.testOptions.apply {
      animationsDisabled = true

      managedDevices {
        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApiExpectedApiLevelDebugAndroidTest
        // e.g. ./gradlew -Pandroid.sdk.channel=3 nexusOneApi36DebugAndroidTest
        API_LEVELS.forEach { apiLevel ->
          localDevices.register("nexusOneApi$apiLevel") {
            device = "Nexus One"
            this.apiLevel = apiLevel
            systemImageSource = if (apiLevel == 37) "google" else "aosp-atd"
            // API 37's default is 16 KB pages, whose graphics allocator stalls badly under
            // the software renderer these tests run on. 37.0 still ships a 4 KB image.
            pageAlignment = if (apiLevel == 37) FORCE_4KB_PAGES else DEFAULT_FOR_SDK_VERSION
            // The tests run on the ABI of the emulator itself. Unless that is set, AGP 10 tests
            // arm64-v8a, which the x86 emulators run translated at best.
            testedAbi = emulatorAbi(apiLevel, osArchitecture)
          }
        }
        // ./gradlew -Pandroid.sdk.channel=3 nexusOneIntegrationTestGroupDebugAndroidTest
        groups.register("nexusOneIntegrationTestGroup") {
          API_LEVELS.forEach { apiLevel -> targetDevices.add(allDevices["nexusOneApi$apiLevel"]) }
        }
      } // managedDevices
    } // testOptions

    // AGP doesn't pass testedAbi on to the task that sets a device up, which checks it and warns
    // that it isn't set.
    val setupTasks = API_LEVELS.associateBy { apiLevel -> "nexusOneApi${apiLevel}Setup" }
    project.tasks
      .named { it in setupTasks }
      .configureEach {
        @Suppress("UNCHECKED_CAST") val testedAbi = property("testedAbi") as Property<String>
        if (!testedAbi.isPresent) {
          testedAbi.set(emulatorAbi(setupTasks.getValue(name), osArchitecture))
        }
      }
  } // apply

  private companion object {
    private val API_LEVELS = 30..37

    /** Returns the ABI of the emulator image that AGP picks for an API level on a machine. */
    private fun emulatorAbi(apiLevel: Int, architecture: CpuArchitecture): String =
      when (architecture) {
        CpuArchitecture.ARM,
        CpuArchitecture.X86_ON_ARM -> "arm64-v8a"
        // Up to API 30, AGP picks a 32-bit image unless the device requires a 64-bit one.
        else -> if (apiLevel <= 30) "x86" else "x86_64"
      }
  }
}
