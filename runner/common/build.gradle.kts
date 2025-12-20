import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.kotlin.module)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.detekt)
}

java {
  sourceSets["main"].java.setSrcDirs(listOf("src/main/kotlin"))
  sourceSets["test"].java.setSrcDirs(listOf("src/test/kotlin"))
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_21 } }

tasks.withType<Test> { useJUnitPlatform() }

dependencies {
  // Core Robolectric dependencies
  api(project(":junit"))
  api(project(":sandbox"))
  api(project(":pluginapi"))
  api(project(":resources"))
  api(project(":utils"))
  implementation(project(":robolectric"))
  implementation(project(":plugins:maven-dependency-resolver"))

  // Kotlin
  implementation(libs.kotlin.stdlib)

  // JUnit Platform for engine integration
  implementation(platform(libs.junit.jupiter.bom))
  implementation(libs.junit.platform.engine)
  implementation(libs.junit4)

  // Compile-only dependencies
  compileOnly(libs.findbugs.jsr305)
  compileOnly(AndroidSdk.MAX_SDK.coordinates)

  // Test dependencies
  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.platform.launcher)
  testImplementation(libs.truth)
  testImplementation(project(":testapp"))
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(androidStubsJar())
  testRuntimeOnly(AndroidSdk.MAX_SDK.preinstrumentedCoordinates)
}

configurations {
  testCompileClasspath {
    attributes.attribute(
      Attribute.of("com.android.build.api.attributes.BuildTypeAttr", String::class.java),
      "debug",
    )
  }
  testRuntimeClasspath {
    attributes.attribute(
      Attribute.of("com.android.build.api.attributes.BuildTypeAttr", String::class.java),
      "debug",
    )
  }
}

fun androidStubsJar(): ConfigurableFileCollection {
  val androidStubsVersion = libs.versions.androidstubs.get()
  val androidHome = System.getenv("ANDROID_HOME")
  require(!androidHome.isNullOrBlank()) { "ANDROID_HOME environment variable not set or blank." }
  val androidJarPath = "$androidHome/platforms/android-$androidStubsVersion/android.jar"
  val androidJar = file(androidJarPath)
  return files(androidJar)
}
