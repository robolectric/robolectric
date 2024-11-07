plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.gradle.managed.device)
}

android {
  compileSdk = 35
  namespace = "org.robolectric.integrationtests.ctesque"

  defaultConfig {
    minSdk = 21
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  lint { abortOnError = false }

  testOptions {
    targetSdk = 35
    unitTests.isIncludeAndroidResources = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  androidResources { noCompress.add("txt") }

  sourceSets {
    val sharedTestDir = "src/sharedTest/"
    val sharedTestSourceDir = sharedTestDir + "java"
    val sharedTestResourceDir = sharedTestDir + "resources"

    val test by getting
    test.resources.srcDirs(sharedTestResourceDir)
    test.java.srcDirs(sharedTestSourceDir)

    val androidTest by getting
    androidTest.resources.srcDirs(sharedTestResourceDir)
    androidTest.java.srcDirs(sharedTestSourceDir)
  }
}

dependencies {
  implementation(project(":testapp"))

  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.androidx.test.monitor)
  testImplementation(libs.androidx.test.runner)
  testImplementation(libs.androidx.test.rules)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.androidx.test.ext.truth)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.androidx.test.espresso.core)
  testImplementation(libs.truth)
  testImplementation(libs.guava)

  // Testing dependencies
  androidTestImplementation(project(":shadowapi"))
  androidTestImplementation(libs.androidx.test.monitor)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.ext.truth)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.truth)
  androidTestImplementation(libs.guava)
  androidTestUtil(libs.androidx.test.services)
}
