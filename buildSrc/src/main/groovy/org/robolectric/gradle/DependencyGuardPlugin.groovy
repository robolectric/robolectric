package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class DependencyGuardPlugin implements Plugin<Project> {
    Closure<Project> doApply = {
        project.apply plugin: "com.dropbox.dependency-guard"

        dependencyGuard {
            def configurationName
            if (project.plugins.hasPlugin("com.android.library")) {
                configurationName = "releaseRuntimeClasspath"
            } else {
                configurationName = "runtimeClasspath"
            }

            configuration(configurationName)
        }
    }

    @Override
    void apply(Project project) {
        doApply.delegate = project
        doApply.resolveStrategy = Closure.DELEGATE_ONLY
        doApply()
    }
}
