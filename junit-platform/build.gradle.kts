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
  api(platform(libs.junit.jupiter.bom))
  api(libs.junit.jupiter.api)
  api(libs.junit.platform.engine)
  implementation(libs.junit.platform.launcher)
  runtimeOnly(libs.junit.jupiter.engine)

  compileOnly(libs.findbugs.jsr305)
  // Only needed to run JUnit 4 classes, which bring JUnit 4 with them.
  compileOnly(libs.junit4)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.platform.testkit)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(variantOf(libs.androidx.test.ext.junit) { artifactType("aar") })
  testRuntimeOnly(libs.junit.vintage)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
}
