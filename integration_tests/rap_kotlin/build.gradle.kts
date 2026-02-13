import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.android.legacy.kapt)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.rap.kotlin"

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
    // rap_kotlin does not support AndroidTest.
    variantBuilder.enableAndroidTest = false
  }
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

// AGP 9 built-in Kotlin disables kotlin-kapt, so use the compatibility plugin.
kapt {
  arguments {
    arg("org.robolectric.annotation.processing.shadowPackage", "org.robolectric.rap.kotlin")
    arg("org.robolectric.annotation.processing.priority", "1")
  }
}

dependencies {
  api(project(":shadows:framework"))
  implementation(libs.kotlin.stdlib)
  kapt(project(":processor"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
