import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.gradle.plugin.publish)
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
}

gradlePlugin {
  website = "https://robolectric.org"
  vcsUrl = "https://github.com/robolectric/robolectric"
  plugins {
    register("kotestPlugin") {
      id = "org.robolectric.kotest"
      displayName = "Robolectric Kotest"
      description =
        "Lets the static initializers of Kotest specs that run in Robolectric use Android in " +
          "the tasks of Kotest's own Gradle plugin"
      implementationClass = "org.robolectric.kotest.gradle.KotestPlugin"
      tags = listOf("android", "robolectric", "kotest")
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  // The tests use JUnit 6, which needs Java 17, while the plugin itself stays on Java 11.
  disableAutoTargetJvm()
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

afterEvaluate {
  val isSnapshotVersion = project.version.toString().endsWith("-SNAPSHOT")
  publishing { signing { setRequired { !isSnapshotVersion } } }
}

tasks.withType<Test> { useJUnitPlatform() }

dependencies {
  implementation(libs.kotlin.stdlib)

  testImplementation(gradleTestKit())
  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.validatePlugins {
  failOnWarning = true
  enableStricterValidation = true
}
