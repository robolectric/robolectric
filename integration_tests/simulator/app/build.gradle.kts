plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.robolectric.spotless)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.integrationtests.simulator"

  defaultConfig {
    applicationId = "org.robolectric.integrationtests.simulator"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes { release { isMinifyEnabled = false } }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  configurations.all {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
  }
}

dependencies {
  implementation(libs.androidx.core)
  implementation(libs.androidx.appcompat)
}
