import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.detekt)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.spotless)
}

android {
  //noinspection GradleDependency keep compileSdk to 30 for compatibility testing purpose
  compileSdk = 30
  namespace = "org.robolectric.integrationtests.sdkcompat"

  defaultConfig { minSdk = 23 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    // We must keep targetSdk to 29 for compatibility testing purpose
    targetSdk = 29
    unitTests.isIncludeAndroidResources = true
  }
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  implementation(libs.kotlin.stdlib)

  testImplementation(project(":testapp"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
