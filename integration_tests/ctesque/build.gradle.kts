import com.android.build.api.dsl.AndroidLibrarySourceSet

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.robolectric.android.project)
  alias(libs.plugins.robolectric.gradle.managed.device)
}

android {
  compileSdk = 37
  namespace = "org.robolectric.integrationtests.ctesque"

  defaultConfig {
    minSdk = 23
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  testOptions {
    targetSdk = 36
    unitTests {
      isIncludeAndroidResources = true
      all { it.systemProperty("robolectric.graphicsMode", "NATIVE") }
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  androidResources { noCompress.add("txt") }

  sourceSets {
    val configurationAction: AndroidLibrarySourceSet.() -> Unit = {
      val sharedTestDir = "src/sharedTest/"

      resources.directories.add(sharedTestDir + "resources")
      java.directories.add(sharedTestDir + "java")
    }

    named("test", configurationAction)
    named("androidTest", configurationAction)
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
