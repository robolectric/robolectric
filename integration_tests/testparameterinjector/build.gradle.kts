plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.integrationtests.testparameterinjector"

  defaultConfig { minSdk = 21 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    targetSdk = 35
    unitTests.isIncludeAndroidResources = true
  }

  androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
      // testparameterinjector does not support AndroidTest.
      variantBuilder.enableAndroidTest = false
    }
  }
}

dependencies {
  // Testing dependencies
  testImplementation(project(":robolectric"))
  testImplementation("com.google.testparameterinjector:test-parameter-injector:1.18@jar")
  testImplementation(libs.findbugs.jsr305)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
