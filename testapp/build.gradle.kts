plugins { alias(libs.plugins.android.library) }

android {
  compileSdk = 30
  namespace = "org.robolectric.testapp"

  defaultConfig {
    minSdk = 23

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  lint { baseline = layout.projectDirectory.file("lint-baseline.xml").asFile }
}
