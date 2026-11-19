import org.robolectric.gradle.AndroidSdk

plugins { alias(libs.plugins.robolectric.java.module) }

// JUnit 6 requires Java 17.
tasks.withType<JavaCompile>().configureEach {
  sourceCompatibility = JavaVersion.VERSION_17.toString()
  targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  // These tests only target the oldest and newest SDKs, so CI shards that enable only other SDKs
  // discover no tests.
  failOnNoDiscoveredTests = false
}

dependencies {
  testImplementation(project(":junit-platform"))
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)

  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)
  testImplementation(libs.mockito.junit.jupiter)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testImplementation(libs.junit.platform.launcher)
  testImplementation(libs.junit4)
  testRuntimeOnly(libs.junit.vintage)
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
