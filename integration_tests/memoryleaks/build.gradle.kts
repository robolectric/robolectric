plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 34
  namespace = "org.robolectric.integrationtests.memoryleaks"

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
  // Testing dependencies
  testImplementation(project(":testapp"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.guava.testlib)
  testImplementation(libs.androidx.fragment)
  testImplementation(libs.truth)
}
