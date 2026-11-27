import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins { alias(libs.plugins.android.application) }

val robolectricVersion = providers.gradleProperty("robolectricVersion").get()

android {
  namespace = "org.robolectric.releasecheck"
  compileSdk { version = release(37) { minorApiLevel = 1 } }

  defaultConfig {
    minSdk = 24
    targetSdk = 36
  }
}

dependencies {
  testImplementation("org.robolectric:robolectric:$robolectricVersion")
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}

tasks.withType<Test>().configureEach {
  testLogging {
    events("passed", "skipped", "failed")
    exceptionFormat = TestExceptionFormat.FULL
  }
}
