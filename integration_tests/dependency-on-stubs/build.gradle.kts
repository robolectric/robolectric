plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

// test with a project that depends on the stubs jar, not org.robolectric:android-all

android {
  compileSdk = 36
  namespace = "org.robolectric"

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
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
}
