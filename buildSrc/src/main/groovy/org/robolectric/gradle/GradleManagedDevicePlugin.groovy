package org.robolectric.gradle

import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleManagedDevicePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.android.testOptions {
            animationsDisabled = true
            devices {
                // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi29DebugAndroidTest
                nexusOneApi29(ManagedVirtualDevice) {
                    device = "Nexus One"
                    apiLevel = 29
                    systemImageSource = "aosp"
                }
                // ./gradlew -Pandroid.sdk.channel=3 nexusOneApi33DebugAndroidTest
                nexusOneApi33(ManagedVirtualDevice) {
                    device = "Nexus One"
                    apiLevel = 33
                    systemImageSource = "google"
                }
            }
            // Disable Emulator snapshots to ensure that we can run tests with UTP on
            // GitHub CI.
            // See https://issuetracker.google.com/issues/198509760.
            emulatorSnapshots {
                enableForTestFailures false
                maxSnapshotsForTestFailures 0
            }
        }
    }
}