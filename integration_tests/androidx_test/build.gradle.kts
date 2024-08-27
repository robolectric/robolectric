plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.gradle.managed.device)
}

android {
  compileSdk = 34
  namespace = "org.robolectric.integration.axt"

  defaultConfig {
    testApplicationId = "org.robolectric.integrationtests.axt"
    minSdk = 21
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArguments["useTestStorageService"] = "true"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    targetSdk = 34
    unitTests.isIncludeAndroidResources = true
  }

  sourceSets {
    val sharedTestDir = "src/sharedTest/"
    val sharedTestSourceDir = sharedTestDir + "java"
    val sharedTestResourceDir = sharedTestDir + "resources"
    val sharedAndroidManifest = sharedTestDir + "AndroidManifest.xml"

    val test by getting
    test.resources.srcDirs(sharedTestResourceDir)
    test.java.srcDirs(sharedTestSourceDir)
    test.manifest.srcFile(sharedAndroidManifest)

    val androidTest by getting
    androidTest.resources.srcDirs(sharedTestResourceDir)
    androidTest.java.srcDirs(sharedTestSourceDir)
    androidTest.manifest.srcFile(sharedAndroidManifest)
  }
}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)

  // Testing dependencies
  testImplementation(project(":robolectric"))
  testImplementation(libs.androidx.test.runner)
  testImplementation(libs.junit4)
  testImplementation(libs.androidx.test.rules)
  testImplementation(libs.androidx.test.espresso.intents)
  testImplementation(libs.androidx.test.espresso.core)
  testImplementation(libs.androidx.test.ext.truth)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.androidx.biometric)
  testImplementation(libs.androidx.fragment)
  testImplementation(libs.androidx.fragment.testing)
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.truth)

  androidTestImplementation(project(":annotations"))
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.junit4)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.espresso.intents)
  androidTestImplementation(libs.androidx.test.espresso.core)
  androidTestImplementation(libs.androidx.test.ext.truth)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(platform(libs.kotlin.bom))
  androidTestImplementation(libs.truth)
  androidTestUtil(libs.androidx.test.services)
}
