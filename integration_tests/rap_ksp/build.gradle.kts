import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ksp)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.rap.ksp"

  defaultConfig { minSdk = 23 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    targetSdk = 36
    unitTests.isIncludeAndroidResources = true
  }
}

androidComponents {
  beforeVariants { variantBuilder ->
    // rap_ksp does not support AndroidTest.
    variantBuilder.enableAndroidTest = false
  }
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

ksp {
  arg("org.robolectric.annotation.processing.shadowPackage", "org.robolectric.rap.ksp")
  arg("org.robolectric.annotation.processing.priority", "1")
}

dependencies {
  api(project(":shadows:framework"))
  implementation(libs.kotlin.stdlib)
  ksp(project(":processor-ksp"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
