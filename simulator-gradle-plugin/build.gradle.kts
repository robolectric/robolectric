plugins {
  alias(libs.plugins.gradle.plugin.publish)
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
}

version = "1.0-SNAPSHOT"

gradlePlugin {
  website.set("https://robolectric.org/simulator")
  vcsUrl.set("https://github.com/robolectric/robolectric")
  plugins {
    create("simulatorPlugin") {
      id = "org.robolectric.simulator"
      displayName = "Robolectric Simulator"
      description =
        "A Robolectric-powered simulator that has the ability to preview and interact with Android apps in a Robolectric environment"
      implementationClass = "org.robolectric.simulator.gradle.SimulatorPlugin"
      tags.set(listOf("android", "robolectric", "simulator"))
    }
  }
}

afterEvaluate {
  val isSnapshotVersion = project.version.toString().endsWith("-SNAPSHOT")
  publishing { signing { setRequired { !isSnapshotVersion } } }
}

dependencies {
  compileOnly(libs.android.gradle.api)
  implementation(libs.kotlin.stdlib)
}
