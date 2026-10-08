package org.robolectric.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.FormatterFunc
import com.diffplug.spotless.FormatterStep
import com.diffplug.spotless.JarState
import com.diffplug.spotless.Provisioner
import com.diffplug.spotless.SerializedFunction
import java.lang.reflect.InvocationTargetException
import java.util.function.Function
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("com.diffplug.spotless")

    project.extensions.configure<SpotlessExtension> {
      // Add configuration for Java files
      java {
        addStep(GoogleJavaFormat.googleJavaFormat("1.37.0"))
        target("**/*.java")
        targetExclude(
          "processor/src/test/resources/org/robolectric/**/*.java",
          "robolectric/src/test/java/org/robolectric/Manifest.java",
          "robolectric/src/test/java/org/robolectric/R.java",
        )
      }

      // Add configurations for Kotlin files
      kotlin {
        target("**/*.kt")
        ktfmt("0.64").googleStyle()
      }

      // Add configurations for Kotlin Gradle files
      kotlinGradle {
        target("**/*.kts")
        ktfmt("0.64").googleStyle()
      }

      // Only apply YAML and JSON formatting for root project
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

/**
 * Runs google-java-format as Spotless's own `googleJavaFormat` step does by default: it formats a
 * source in the Google style, then removes its unused imports.
 *
 * Spotless can't run google-java-format 1.37.0, where `JavaFormatterOptions.Style` isn't an enum
 * anymore, see https://github.com/diffplug/spotless/issues/3126. Use Spotless's step again once it
 * can.
 */
private class GoogleJavaFormat(classLoader: ClassLoader) : FormatterFunc {
  private val formatter = classLoader.loadClass("$PACKAGE.Formatter").getConstructor().newInstance()
  private val formatSource = formatter.javaClass.getMethod("formatSource", String::class.java)
  private val removeUnusedImports =
    classLoader
      .loadClass("$PACKAGE.RemoveUnusedImports")
      .getMethod("removeUnusedImports", String::class.java)

  override fun apply(input: String): String =
    try {
      removeUnusedImports.invoke(null, formatSource.invoke(formatter, input)) as String
    } catch (e: InvocationTargetException) {
      throw e.cause ?: e
    }

  companion object {
    private const val PACKAGE = "com.google.googlejavaformat.java"

    /** Creates the step. Renovate updates the version by the name of this function. */
    fun googleJavaFormat(version: String) =
      Function<Provisioner, FormatterStep> { provisioner ->
        FormatterStep.create(
          "google-java-format",
          JarState.promise {
            JarState.from("com.google.googlejavaformat:google-java-format:$version", provisioner)
          },
          SerializedFunction(JarState.Promised::get),
          SerializedFunction { jarState ->
            openJavacPackages()
            GoogleJavaFormat(jarState.classLoader)
          },
        )
      }

    /**
     * Lets google-java-format use the packages of javac that it formats with, as Spotless's own
     * step does. Otherwise the Gradle daemon would need an `--add-exports` argument for each.
     */
    private fun openJavacPackages() {
      FormatterStep::class
        .java
        .classLoader
        .loadClass("com.diffplug.spotless.java.ModuleHelper")
        .getDeclaredMethod("doOpenInternalPackagesIfRequired")
        .apply { isAccessible = true }
        .invoke(null)
    }
  }
}
