package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class SpotlessPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.getPlugins().apply('com.diffplug.spotless')

        project.spotless {
            // Add configurations for Kotlin files
            kotlin {
                target("**/*.kt")
                ktfmt("0.49").googleStyle()
            }

            // Add configurations for Kotlin Gradle files
            kotlinGradle {
                target("**/*.kts")
                ktfmt("0.49").googleStyle()
            }

            // Add configurations for Groovy files
            groovy {
                target("**/*.groovy")
            }

            // Add configurations for Groovy Gradle files
            groovyGradle {
                target("*.gradle", "**/*.gradle")
            }
        }
    }
}