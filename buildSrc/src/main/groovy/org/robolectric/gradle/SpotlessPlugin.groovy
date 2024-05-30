package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpotlessPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.getPlugins().apply('com.diffplug.spotless')

        project.spotless {
            kotlin {
                // Add configurations for Kotlin files
                target '**/*.kt'
                ktfmt('0.49').googleStyle()
            }
            groovy {
                // Add configurations for Groovy files
                target("**/*.groovy")
            }
            groovyGradle {
                // Add configurations for Groovy Gradle files
                target('*.gradle', "**/*.gradle")
            }
        }
    }
}