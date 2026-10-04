import org.robolectric.gradle.AndroidSdk
import org.robolectric.gradle.AttributeNames

plugins {
  alias(libs.plugins.robolectric.deployed.kotlin.module)
  alias(libs.plugins.robolectric.kotlin.module)
  alias(libs.plugins.detekt)
}

kotlin {
  // Nothing is part of the API unless it is declared public.
  explicitApi()
}

tasks.withType<Test> { useJUnitPlatform() }

dependencies {
  // The API has Robolectric's Config and Configuration in it.
  api(project(":annotations"))
  api(project(":pluginapi"))
  implementation(project(":robolectric"))
  implementation(libs.kotlin.stdlib)

  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.truth)
  testImplementation(project(":testapp"))
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(androidStubsJar())
  testRuntimeOnly(AndroidSdk.MAX_SDK.preinstrumentedCoordinates)
}

configurations {
  testCompileClasspath {
    attributes.attribute(Attribute.of(AttributeNames.BUILD_TYPE_ATTR, String::class.java), "debug")
  }
  testRuntimeClasspath {
    attributes.attribute(Attribute.of(AttributeNames.BUILD_TYPE_ATTR, String::class.java), "debug")
  }
}

fun androidStubsJar(): ConfigurableFileCollection {
  val androidStubsVersion = libs.versions.androidstubs.get()
  val androidHome = System.getenv("ANDROID_HOME")
  if (androidHome.isNullOrBlank()) {
    throw GradleException("ANDROID_HOME environment variable not set or blank.")
  }
  return files("$androidHome/platforms/android-$androidStubsVersion/android.jar")
}
