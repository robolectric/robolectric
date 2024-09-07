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

    @Suppress("UnstableApiUsage")
    androidExtension.testOptions {
      animationsDisabled = true

      managedDevices {
        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi29DebugAndroidTest
        localDevices.create(NAME_API_29) {
          device = "Nexus One"
          apiLevel = 29
          systemImageSource = "aosp"
        }

        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi30DebugAndroidTest
        localDevices.create(NAME_API_30) {
          device = "Nexus One"
          apiLevel = 30
          systemImageSource = "aosp-atd"
        }

        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi31DebugAndroidTest
        localDevices.create(NAME_API_31) {
          device = "Nexus One"
          apiLevel = 31
          systemImageSource = "aosp-atd"
        }

        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi32DebugAndroidTest
        localDevices.create(NAME_API_32) {
          device = "Nexus One"
          apiLevel = 32
          systemImageSource = "aosp-atd"
        }

        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi33DebugAndroidTest
        localDevices.create(NAME_API_33) {
          device = "Nexus One"
          apiLevel = 33
          systemImageSource = "aosp-atd"
        }

        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi34DebugAndroidTest
        localDevices.create(NAME_API_34) {
          device = "Nexus One"
          apiLevel = 34
          systemImageSource = "aosp-atd"
        }

        // ./gradlew -Pandroid.sdk.channel=3 nexusOneIntegrationTestGroupDebugAndroidTest
        groups.create("nexusOneIntegrationTestGroup") {
          targetDevices.add(devices[NAME_API_29])
          targetDevices.add(devices[NAME_API_30])
          targetDevices.add(devices[NAME_API_31])
          targetDevices.add(devices[NAME_API_32])
          targetDevices.add(devices[NAME_API_33])
          targetDevices.add(devices[NAME_API_34])
        }
      } // managedDevices
    } // testOptions
  } // apply

  companion object {
    const val NAME_API_29 = "nexusOneApi29"
    const val NAME_API_30 = "nexusOneApi30"
    const val NAME_API_31 = "nexusOneApi31"
    const val NAME_API_32 = "nexusOneApi32"
    const val NAME_API_33 = "nexusOneApi33"
    const val NAME_API_34 = "nexusOneApi34"
  }
}
