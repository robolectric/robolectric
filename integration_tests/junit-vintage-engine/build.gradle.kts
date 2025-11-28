import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.spotless)
}

android {
  namespace = "org.robolectric.integrationtests.junit.vintage"
  compileSdk = 36

  defaultConfig { minSdk = 23 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    targetSdk = 36
    unitTests.isIncludeAndroidResources = true
  }

  androidComponents {
    beforeVariants { variantBuilder ->
      // junit-vintage-engine test module does not support AndroidTest now.
      variantBuilder.enableAndroidTest = false
    }
  }
}

tasks.withType<Test> { useJUnitPlatform() }

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  implementation(project(":testapp"))

  testImplementation(project(":robolectric"))
  testImplementation(libs.kotlin.stdlib)
  testImplementation(libs.junit4)

  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.vintage)
  testImplementation(libs.junit.platform.launcher)
}
