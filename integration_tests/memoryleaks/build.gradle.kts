plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk { version = release(37) { minorApiLevel = 1 } }
  namespace = "org.robolectric.integrationtests.memoryleaks"

  defaultConfig { minSdk = 24 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    targetSdk = 36
    unitTests.isIncludeAndroidResources = true
  }
}

androidComponents {
  beforeVariants { variantBuilder ->
    // memoryleaks does not support AndroidTest.
    variantBuilder.enableAndroidTest = false
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
