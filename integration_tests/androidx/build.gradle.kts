plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 35
  namespace = "org.robolectric.integrationtests.androidx"

  defaultConfig { minSdk = 21 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    targetSdk = 35
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
