import org.robolectric.gradle.AndroidSdk

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

  lint { abortOnError = false }

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

afterEvaluate {
  val testDebugUnitTest = tasks.named<Test>("testDebugUnitTest").get()
  val testDebugUnitTestNativeResources =
    tasks.register<Test>("testDebugUnitTestNativeResources") {
      testClassesDirs = testDebugUnitTest.testClassesDirs
      classpath = testDebugUnitTest.classpath
      systemProperty("robolectric.graphicsMode", "NATIVE")
      systemProperty("robolectric.resourcesMode", "NATIVE")
      val nativeResourcesSdks =
        AndroidSdk.ALL_SDKS.filter { it.apiLevel >= AndroidSdk.BAKLAVA.apiLevel }
          .joinToString(",") { it.apiLevel.toString() }
      systemProperty("robolectric.enabledSdks", nativeResourcesSdks)
      filter {
        isFailOnNoMatchingTests = false
        includeTestsMatching("android.content.res.AssetManagerTest")
        includeTestsMatching("android.content.res.ResourcesTest")
        includeTestsMatching("android.content.res.ThemeTest")
      }
    }

  tasks.named("test") { dependsOn(testDebugUnitTestNativeResources) }
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
