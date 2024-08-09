package org.robolectric.gradle

import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

class GradleManagedDevicePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("com.android.library")

    project.extensions.configure<LibraryExtension> {
      @Suppress("UnstableApiUsage")
      testOptions {
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
}
