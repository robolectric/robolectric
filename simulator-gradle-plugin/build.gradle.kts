import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.gradle.plugin.publish)
  `kotlin-dsl`
  `java-gradle-plugin`
  `maven-publish`
  signing
}

version = "1.0-SNAPSHOT"

gradlePlugin {
  website = "https://robolectric.org/simulator"
  vcsUrl = "https://github.com/robolectric/robolectric"
  plugins {
    register("simulatorPlugin") {
      id = "org.robolectric.simulator"
      displayName = "Robolectric Simulator"
      description =
        "A Robolectric-powered simulator that has the ability to preview and interact with Android apps in a Robolectric environment"
      implementationClass = "org.robolectric.simulator.gradle.SimulatorPlugin"
      tags = listOf("android", "robolectric", "simulator")
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  // AGP 9.3+ publishes gradle-api for Java 17. Lift the JVM-version constraint on the
  // compile classpath so the latest API can be used while this module keeps emitting
  // and advertising Java 11. Safe because gradle-api is compileOnly.
  disableAutoTargetJvm()
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

afterEvaluate {
  val isSnapshotVersion = project.version.toString().endsWith("-SNAPSHOT")
  publishing { signing { setRequired { !isSnapshotVersion } } }
}

// The Android Gradle plugin, for the projects that the Gradle TestKit tests build. The plugin only
// compiles against AGP's API, so TestKit has to put AGP itself on the plugin classpath it injects.
val testKitAndroidGradlePlugin by configurations.creating

dependencies {
  compileOnly(libs.android.gradle.api)

  testImplementation(gradleTestKit())
  testImplementation(libs.junit4)
  testImplementation(libs.test.parameter.injector)
  testImplementation(libs.truth)

  testKitAndroidGradlePlugin(libs.android.gradle)
}

tasks.pluginUnderTestMetadata { pluginClasspath.from(testKitAndroidGradlePlugin) }

tasks.test {
  // PublicationTest checks the Gradle module metadata that gets published for the plugin.
  val moduleMetadata =
    tasks.named<GenerateModuleMetadata>("generateMetadataFileForPluginMavenPublication")
  val moduleMetadataFile = moduleMetadata.flatMap { it.outputFile }
  inputs.file(moduleMetadataFile).withPropertyName("moduleMetadata")
  jvmArgumentProviders.add(
    CommandLineArgumentProvider {
      listOf("-Dsimulator.plugin.moduleMetadata=${moduleMetadataFile.get().asFile.path}")
    },
  )

  // The TestKit builds need an Android SDK, from ANDROID_HOME or the root local.properties.
  val localProperties = rootProject.file("local.properties")
  if (localProperties.exists()) {
    val sdkDir =
      localProperties
        .readLines()
        .firstOrNull { it.startsWith("sdk.dir=") }
        ?.removePrefix("sdk.dir=")
    if (sdkDir != null) systemProperty("android.sdk.dir", sdkDir)
  }
}

tasks.validatePlugins {
  failOnWarning = true
  enableStricterValidation = true
}
