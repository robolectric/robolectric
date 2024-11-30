plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

// test with a project that depends on the stubs jar, not org.robolectric:android-all

android {
  compileSdk = 35
  namespace = "org.robolectric"

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
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
}
