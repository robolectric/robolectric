plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.spotless)
  alias(libs.plugins.roborazzi)
}

android {
  compileSdk = 35
  namespace = "org.robolectric.integration.roborazzi"

  defaultConfig { minSdk = 21 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions { jvmTarget = "1.8" }

  testOptions {
    targetSdk = 35
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

dependencies {
  api(project(":robolectric"))
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.junit4)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.rule)
}
