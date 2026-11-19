import org.robolectric.gradle.AndroidSdk

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

// JUnit 6 requires Java 17.
tasks.withType<JavaCompile>().configureEach {
  sourceCompatibility = JavaVersion.VERSION_17.toString()
  targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  api(project(":robolectric"))
  // Built against the oldest supported JUnit, which the published metadata then asks for, so that
  // apps on a newer JUnit 5 or on JUnit 6 keep their own version.
  api(platform(libs.junit5.bom))
  api(libs.junit.jupiter.api)
  api(libs.junit.platform.engine)
  implementation(libs.junit.platform.launcher)
  runtimeOnly(libs.junit.jupiter.engine)

  compileOnly(libs.findbugs.jsr305)

  // The tests run on JUnit 6, and CI also runs them on JUnit 5.
  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.platform.testkit)
  testImplementation(libs.truth)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
}

// CI also runs these tests on JUnit 5 with -PjunitVersion=<version>.
providers.gradleProperty("junitVersion").orNull?.let { junitVersion ->
  // JUnit 5.x.y ships the JUnit Platform as 1.x.y.
  val platformVersion = junitVersion.replaceFirst(Regex("^5\\."), "1.")
  configurations.configureEach {
    resolutionStrategy.eachDependency {
      when (requested.group) {
        "org.junit",
        "org.junit.jupiter",
        "org.junit.vintage" -> useVersion(junitVersion)
        "org.junit.platform" -> useVersion(platformVersion)
      }
    }
  }
}
