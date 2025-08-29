plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.rap"

  defaultConfig { minSdk = 21 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    targetSdk = 35
    unitTests.isIncludeAndroidResources = true
  }

  androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
      // rap does not support AndroidTest.
      variantBuilder.enableAndroidTest = false
    }
  }
}

dependencies {
  annotationProcessor(project(":processor"))
  api(project(":shadows:framework"))
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}

tasks.withType<JavaCompile>().configureEach {
  options.compilerArgs.add(
    "-Aorg.robolectric.annotation.processing.shadowPackage=org.robolectric.rap"
  )
  options.compilerArgs.add("-Aorg.robolectric.annotation.processing.priority=1")
}
