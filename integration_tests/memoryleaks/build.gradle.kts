plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.integrationtests.memoryleaks"

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
