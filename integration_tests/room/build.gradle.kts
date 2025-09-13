plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.integrationtests.room"

  defaultConfig { minSdk = 23 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    targetSdk = 36
    unitTests.isIncludeAndroidResources = true
  }

  androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
      // room does not support AndroidTest.
      variantBuilder.enableAndroidTest = false
    }
  }
}

dependencies {
  // Testing dependencies
  testImplementation(project(":testapp"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  implementation(libs.androidx.room.runtime)
  annotationProcessor(libs.androidx.room.compiler)
}
