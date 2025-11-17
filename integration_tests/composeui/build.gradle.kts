import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.spotless)
}

android {
  namespace = "org.robolectric.integrationtests.composeui"
  compileSdk = 36

  defaultConfig { minSdk = 23 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures { compose = true }

  testOptions {
    targetSdk = 36
    unitTests.isIncludeAndroidResources = true
  }

  androidComponents {
    beforeVariants { variantBuilder ->
      // composeui does not support AndroidTest now.
      variantBuilder.enableAndroidTest = false
    }
  }
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.material3)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.androidx.compose.ui.test.junit4)
}
