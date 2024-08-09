package org.robolectric.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType

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
        allDevices.create<ManagedVirtualDevice>("nexusOneApi29") {
          device = "Nexus One"
          apiLevel = 29
          systemImageSource = "aosp"
        }

        // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi34DebugAndroidTest
        allDevices.create<ManagedVirtualDevice>("nexusOneApi34") {
          device = "Nexus One"
          apiLevel = 34
          systemImageSource = "aosp-atd"
        }
      }
    }
  }
}
