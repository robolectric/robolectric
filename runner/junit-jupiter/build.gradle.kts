import org.gradle.api.attributes.Attribute
import org.robolectric.gradle.AndroidSdk
import org.robolectric.gradle.AttributeNames

plugins {
  alias(libs.plugins.robolectric.deployed.kotlin.module)
  alias(libs.plugins.robolectric.kotlin.module)
  alias(libs.plugins.detekt)
}

// Each test source set is owned by exactly one Test task, so a test executes only when its target
// task runs — membership is structural, with no name-based filters:
// - src/test: tests for the custom robolectric-junit-jupiter-engine (tasks.test).
// - src/jupiterExtensionTest: RobolectricExtension tests and engine meta-tests on the standard
//   junit-jupiter engine (jupiterExtensionTest task).
// - src/engineFixtures: fixture classes driven programmatically by the meta-tests; on the
//   jupiterExtensionTest classpath but in no task's testClassesDirs, so they execute only when a
//   driving test launches them.
val engineFixtures =
  sourceSets.register("engineFixtures") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
  }

val jupiterExtensionTestSourceSet =
  sourceSets.register("jupiterExtensionTest") {
    compileClasspath += sourceSets.main.get().output + engineFixtures.get().output
    runtimeClasspath += sourceSets.main.get().output + engineFixtures.get().output
  }

tasks.test { useJUnitPlatform { includeEngines("robolectric-junit-jupiter-engine") } }

val jupiterExtensionTest =
  tasks.register<Test>("jupiterExtensionTest") {
    description =
      "Runs RobolectricExtension tests and engine meta-tests on the standard JUnit Jupiter engine"
    group = "verification"
    testClassesDirs = jupiterExtensionTestSourceSet.get().output.classesDirs
    classpath = jupiterExtensionTestSourceSet.get().runtimeClasspath
    useJUnitPlatform { includeEngines("junit-jupiter") }
    // These tests pin their own SDK sets and assert the canonical per-SDK display names
    // (e.g. "runsOnBoth[33]" with an unmarked last variant); the CI matrix's enabledSdks
    // filter and alwaysIncludeVariantMarkersInTestName flag must not reshape them. Gradle
    // moves -D entries from jvmArgs into systemProperties, so the removal happens there.
    doFirst {
      systemProperties.remove("robolectric.enabledSdks")
      systemProperties.remove("robolectric.alwaysIncludeVariantMarkersInTestName")
    }
  }

tasks.test { dependsOn(jupiterExtensionTest) }

detekt {
  source.from("src/jupiterExtensionTest/kotlin", "src/engineFixtures/kotlin")
  // detekt.yml only widens the default test-folder excludes to the source sets above.
  buildUponDefaultConfig = true
  config.setFrom(files("detekt.yml"))
}

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
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(project(":testapp"))
  testImplementation(libs.truth)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testImplementation(libs.androidx.lifecycle.runtime)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(androidStubsJar())
  // Runtime dependency for preinstrumented jars - downloaded automatically by
  // MavenDependencyResolver
  testRuntimeOnly(AndroidSdk.MAX_SDK.preinstrumentedCoordinates)
}

configurations {
  // The extension-test and fixture source sets share the test dependency scopes.
  named("engineFixturesImplementation") { extendsFrom(named("testImplementation")) }
  named("engineFixturesCompileOnly") { extendsFrom(named("testCompileOnly")) }
  named("jupiterExtensionTestImplementation") { extendsFrom(named("testImplementation")) }
  named("jupiterExtensionTestCompileOnly") { extendsFrom(named("testCompileOnly")) }
  named("jupiterExtensionTestRuntimeOnly") { extendsFrom(named("testRuntimeOnly")) }

  listOf(
      "testCompileClasspath",
      "testRuntimeClasspath",
      "engineFixturesCompileClasspath",
      "engineFixturesRuntimeClasspath",
      "jupiterExtensionTestCompileClasspath",
      "jupiterExtensionTestRuntimeClasspath",
    )
    .forEach { classpath ->
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
  val androidJarPath = "$androidHome/platforms/android-$androidStubsVersion/android.jar"
  val androidJar = file(androidJarPath)
  return files(androidJar)
}
