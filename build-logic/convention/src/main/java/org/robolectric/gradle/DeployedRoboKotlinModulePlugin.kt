package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Publishing plugin for modules that contain Kotlin sources alongside Java.
 *
 * Identical to [DeployedRoboJavaModulePlugin] except the sources JAR includes all source files
 * (`allSource`) instead of only Java files (`allJava`), so that `.kt` sources are packaged for IDE
 * navigation and Maven Central requirements.
 */
class DeployedRoboKotlinModulePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.configureDeployedModule { sourceSets.getByName("main").allSource }
  }
}
