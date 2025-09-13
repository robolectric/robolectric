import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.spotless)
  alias(libs.plugins.roborazzi)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.integration.roborazzi"

  defaultConfig { minSdk = 23 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    targetSdk = 36
    unitTests {
      isIncludeAndroidResources = true
      all {
        // For Roborazzi users, please use Roborazzi plugin and gradle.properties instead of this.
        // https://takahirom.github.io/roborazzi/how-to-use.html#roborazzi-gradle-properties-options

        // Change naming strategy of screenshots.
        // org.robolectric.....RoborazziCaptureTest.checkDialogRendering.png ->
        // RoborazziCaptureTest.checkDialogRendering.png
        it.systemProperty("roborazzi.record.namingStrategy", "testClassAndMethod")

        // Use RoborazziRule's base path when you use captureRoboImage(path).
        it.systemProperty(
          "roborazzi.record.filePathStrategy",
          "relativePathFromRoborazziContextOutputDirectory",
        )
      }
    }
  }

  androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
      // Roborazzi does not support AndroidTest.
      variantBuilder.enableAndroidTest = false
    }
  }
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  api(project(":robolectric"))
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.junit4)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.rule)
}
