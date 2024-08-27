plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.spotless)
}

android {
  //noinspection GradleDependency keep compileSdk to 29 for compatibility testing purpose
  compileSdk = 29
  namespace = "org.robolectric.integrationtests.sdkcompat"

  defaultConfig { minSdk = 21 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions { jvmTarget = "1.8" }

  testOptions {
    // We must keep targetSdk to 29 for compatibility testing purpose
    targetSdk = 29
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  implementation(libs.kotlin.stdlib)

  testImplementation(project(":testapp"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
