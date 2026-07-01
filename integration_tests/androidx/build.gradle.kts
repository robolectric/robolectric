plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.integrationtests.androidx"

  defaultConfig { minSdk = 23 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    targetSdk = 36
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.androidx.window)

  // Testing dependencies
  testImplementation(project(":testapp"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.recyclerview)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.androidx.test.espresso.core)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.truth)
}
