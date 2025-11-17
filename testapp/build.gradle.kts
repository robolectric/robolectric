plugins { alias(libs.plugins.android.library) }

android {
  compileSdk = 36
  namespace = "org.robolectric.testapp"

  defaultConfig {
    minSdk = 21

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  lint {
    abortOnError = false
    targetSdk = 34
  }
}
