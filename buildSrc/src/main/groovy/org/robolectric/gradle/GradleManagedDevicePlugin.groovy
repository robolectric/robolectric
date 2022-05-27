package org.robolectric.gradle

import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleManagedDevicePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.android.testOptions {
            devices {
                // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi29DebugAndroidTest
                nexusOneApi29(ManagedVirtualDevice) {
                    device = "Nexus One"
                    apiLevel = 29
                    systemImageSource = "aosp"
                    abi = "x86"
                }
                // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi32DebugAndroidTest
                nexusOneApi32(ManagedVirtualDevice) {
                    device = "Nexus One"
                    apiLevel = 32
                    systemImageSource = "google"
                    abi = "x86_64"
                }
            }
        }
    }
}