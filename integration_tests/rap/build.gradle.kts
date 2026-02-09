plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
}

android {
  compileSdk = 36
  namespace = "org.robolectric.rap"

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
    // rap does not support AndroidTest.
    variantBuilder.enableAndroidTest = false
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
