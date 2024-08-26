plugins { alias(libs.plugins.android.library) }

android {
  compileSdk = 34
  namespace = "org.robolectric.integrationtests.agp.testsupport"

  defaultConfig { minSdk = 21 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions.targetSdk = 34
}

dependencies { api(project(":integration_tests:agp")) }
