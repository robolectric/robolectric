import org.robolectric.gradle.AndroidSdk

plugins { alias(libs.plugins.robolectric.java.module) }

// JUnit 6 requires Java 17.
tasks.withType<JavaCompile>().configureEach {
  sourceCompatibility = JavaVersion.VERSION_17.toString()
  targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

dependencies {
  testImplementation(project(":junit-platform"))
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)

  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)
  testImplementation(libs.mockito.junit.jupiter)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testImplementation(variantOf(libs.androidx.test.ext.junit) { artifactType("aar") })
  // JUnit 4 classes are run by the robolectric engine, not by JUnit Vintage.
  testRuntimeOnly(libs.junit.vintage)
  testRuntimeOnly(libs.junit.platform.launcher)
}
