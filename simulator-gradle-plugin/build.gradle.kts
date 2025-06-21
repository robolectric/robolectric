import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.gradle.plugin.publish)
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
}

version = "1.0.2"

gradlePlugin {
  website = "https://robolectric.org/simulator"
  vcsUrl = "https://github.com/robolectric/robolectric"
  plugins {
    register("simulatorPlugin") {
      id = "org.robolectric.simulator"
      displayName = "Robolectric Simulator"
      description =
        "A Robolectric-powered simulator that has the ability to preview and interact with Android apps in a Robolectric environment"
      implementationClass = "org.robolectric.simulator.gradle.SimulatorPlugin"
      tags = listOf("android", "robolectric", "simulator")
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

afterEvaluate {
  val isSnapshotVersion = project.version.toString().endsWith("-SNAPSHOT")
  publishing { signing { setRequired { !isSnapshotVersion } } }
}

dependencies {
  compileOnly(libs.android.gradle.api)
  implementation(libs.kotlin.stdlib)
}

tasks.validatePlugins {
  failOnWarning = true
  enableStricterValidation = true
}
