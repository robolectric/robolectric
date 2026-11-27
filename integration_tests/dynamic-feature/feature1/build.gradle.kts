import org.robolectric.gradle.AndroidSdk

plugins {
  alias(libs.plugins.android.dynamic.feature)
  alias(libs.plugins.robolectric.spotless)
}

android {
  compileSdk { version = release(37) { minorApiLevel = 1 } }
  namespace = "org.robolectric.integrationtests.dynamicfeature.feature1"

  defaultConfig { minSdk = 26 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  implementation(project(":integration_tests:dynamic-feature:app"))

  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}

apply(from = "../base-app-test-config.gradle.kts")
