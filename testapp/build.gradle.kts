plugins { alias(libs.plugins.android.library) }

android {
  compileSdk = 34
  namespace = "org.robolectric.testapp"

  defaultConfig {
    minSdk = 21

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables.useSupportLibrary = true
  }

  lint {
    abortOnError = false
    targetSdk = 34
  }
}
