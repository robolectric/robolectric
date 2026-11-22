plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.robolectric.spotless)
}

android {
  compileSdk = 37
  namespace = "org.robolectric.integrationtests.dynamicfeature"

  defaultConfig {
    applicationId = "org.robolectric.integrationtests.dynamicfeature"
    // With minSdk 26 or higher, AGP assigns dynamic features resource package IDs from 0x80 up.
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  dynamicFeatures +=
    setOf(
      ":integration_tests:dynamic-feature:feature1",
      ":integration_tests:dynamic-feature:feature2",
    )

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
