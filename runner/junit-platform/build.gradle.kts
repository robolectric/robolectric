import org.gradle.api.attributes.Attribute
import org.robolectric.gradle.AttributeNames

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.kotlin.module)
  alias(libs.plugins.kotlin.jvm)
}

tasks.withType<Test> { useJUnitPlatform { includeEngines("robolectric-junit-platform-engine") } }

dependencies {
  api(project(":junit"))
  api(project(":sandbox"))
  api(project(":pluginapi"))
  api(project(":resources"))
  api(project(":utils"))
  implementation(project(":robolectric"))
  implementation(project(":runner:common"))
  // Include maven-dependency-resolver for automatic SDK jar downloading
  implementation(project(":plugins:maven-dependency-resolver"))
  implementation(libs.kotlin.stdlib)
  implementation(platform(libs.junit.jupiter.bom))
  implementation(libs.junit.jupiter)
  implementation(libs.junit.platform.launcher)
  implementation(libs.junit.platform.engine)
  implementation(libs.junit4)
  testImplementation(project(":testapp"))
  testImplementation(libs.truth)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(androidStubsJar())
  // Runtime dependency for preinstrumented jars - downloaded automatically by
  // MavenDependencyResolver
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
  require(!androidHome.isNullOrBlank()) { "ANDROID_HOME environment variable not set or blank." }
  val androidJarPath = "$androidHome/platforms/android-$androidStubsVersion/android.jar"
  val androidJar = file(androidJarPath)
  return files(androidJar)
}
