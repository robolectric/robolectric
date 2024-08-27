plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 34
  namespace = "org.robolectric.shadows"

  defaultConfig { minSdk = 26 }

  testOptions {
    targetSdk = 34
    unitTests {
      isIncludeAndroidResources = true
      all { it.systemProperty("robolectric.graphicsMode", "NATIVE") }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  testImplementation(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(project(":robolectric"))

  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.truth)
  testImplementation(libs.junit4)
  testImplementation(libs.mockito)
  testImplementation(libs.guava.testlib)
}
