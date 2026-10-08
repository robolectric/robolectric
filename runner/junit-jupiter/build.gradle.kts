import org.gradle.api.attributes.Attribute
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

tasks.withType<Test> { useJUnitPlatform { includeEngines("junit-jupiter") } }

dependencies {
  api(platform(libs.junit.jupiter.bom))
  api(libs.junit.jupiter)
  api(project(":robolectric"))
  implementation(project(":runner:common"))
  implementation(libs.kotlin.stdlib)

  testImplementation(libs.junit.platform.launcher)
  testImplementation(project(":testapp"))
  testImplementation(libs.truth)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testImplementation(libs.androidx.lifecycle.runtime)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(androidStubsJar())
  testRuntimeOnly(AndroidSdk.MAX_SDK.preinstrumentedCoordinates)
}

configurations {
  listOf("testCompileClasspath", "testRuntimeClasspath").forEach { classpath ->
    named(classpath) {
      attributes.attribute(
        Attribute.of(AttributeNames.BUILD_TYPE_ATTR, String::class.java),
        "debug",
      )
    }
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
