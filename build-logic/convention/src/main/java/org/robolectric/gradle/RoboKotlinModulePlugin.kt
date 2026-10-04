package org.robolectric.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/** Configures a Kotlin module like [RoboJavaModulePlugin], but targets Java 17. */
class RoboKotlinModulePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("org.jetbrains.kotlin.jvm")
    project.apply<RoboJavaModulePlugin>()

    project.tasks.withType<JavaCompile>().configureEach {
      sourceCompatibility = JavaVersion.VERSION_17.toString()
      targetCompatibility = JavaVersion.VERSION_17.toString()
    }
    project.tasks.withType<KotlinJvmCompile>().configureEach {
      compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
  }
}
