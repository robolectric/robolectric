plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 37
  namespace = "org.robolectric.integrationtests.commonslogging"

  defaultConfig { minSdk = 24 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    targetSdk = 36
    unitTests.isIncludeAndroidResources = true
  }
}

dependencies {
  implementation(libs.commons.logging)
  implementation(libs.xml.security)

  testImplementation(project(":robolectric"))
  testImplementation(libs.commons.logging)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
