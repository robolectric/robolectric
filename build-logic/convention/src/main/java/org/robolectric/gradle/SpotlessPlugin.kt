package org.robolectric.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("com.diffplug.spotless")

    project.extensions.configure<SpotlessExtension> {
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

      // Only apply yaml and json formatting for root project
      // to avoid some files are added into multiple project's spotless targets.
      if (project.rootProject == project) {
        // Add configurations for JSON files
        json {
          target("**/*.json")
          gson()
            .indentWithSpaces(2) // Follow code's indent.
            .sortByKeys()
            .escapeHtml()
        }
      }
    }
  }
}
