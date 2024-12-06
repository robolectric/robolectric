plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 34
  namespace = "org.robolectric.integrationtests.testparameterinjector"

  defaultConfig { minSdk = 21 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    targetSdk = 34
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  // Testing dependencies
  testImplementation(project(":robolectric"))
  testImplementation("com.google.testparameterinjector:test-parameter-injector:1.18@jar")
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
