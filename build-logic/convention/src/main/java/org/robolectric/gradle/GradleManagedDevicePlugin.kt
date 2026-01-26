package org.robolectric.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
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
        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi`ExpectedApiLevelDebugAndroidTest
        // e.g. ./gradlew -Pandroid.sdk.channel=3 nexusOneApi36DebugAndroidTest
        API_LEVELS.forEach { apiLevel ->
          localDevices.register("nexusOneApi$apiLevel") {
            device = "Nexus One"
            this.apiLevel = apiLevel
            systemImageSource = "aosp-atd"
          }
        }
        // ./gradlew -Pandroid.sdk.channel=3 nexusOneIntegrationTestGroupDebugAndroidTest
        groups.register("nexusOneIntegrationTestGroup") {
          API_LEVELS.forEach { apiLevel -> targetDevices.add(allDevices["nexusOneApi$apiLevel"]) }
        }
      } // managedDevices
    } // testOptions
  } // apply

  private companion object {
    private val API_LEVELS = 30..36
  }
}
