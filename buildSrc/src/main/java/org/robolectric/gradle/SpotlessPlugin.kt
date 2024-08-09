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

      // Add configurations for Groovy files
      groovy { target("**/*.groovy") }

      // Add configurations for Groovy Gradle files
      groovyGradle { target("*.gradle", "**/*.gradle") }
    }
  }
}
