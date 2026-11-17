pluginManagement {
  includeBuild("build-logic")
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

  repositories {
    // The android-all jars are hundreds of MB each, and CI downloads them on every run. Maven
    // Central rate limits them with 429 responses when many jobs run at once, so get them from
    // Google's mirror of Maven Central first. The mirror syncs about once a day, so a jar it
    // doesn't have yet is still found on Maven Central after the mirror returns a 404.
    maven("https://maven-central.storage-download.googleapis.com/maven2/") {
      name = "MavenCentralGoogleMirror"
      content {
        includeModule("org.robolectric", "android-all")
        includeModule("org.robolectric", "android-all-instrumented")
      }
    }
    google()
    mavenCentral()
  }
}

rootProject.name = "robolectric-root"

include(
  ":annotations",
  ":errorprone",
  ":integration_tests:androidx",
  ":integration_tests:androidx_test",
  ":integration_tests:commonslogging",
  ":integration_tests:composeui",
  ":integration_tests:ctesque",
  ":integration_tests:dependency-on-stubs",
  ":integration_tests:firebase",
  ":integration_tests:jacoco-offline",
  ":integration_tests:junit-vintage-engine",
  ":integration_tests:kotlin",
  ":integration_tests:libphonenumber",
  ":integration_tests:memoryleaks",
  ":integration_tests:mockito",
  ":integration_tests:mockito-experimental",
  ":integration_tests:mockito-kotlin",
  ":integration_tests:mockk",
  ":integration_tests:nativegraphics",
  ":integration_tests:play_services",
  ":integration_tests:powermock",
  ":integration_tests:rap",
  ":integration_tests:rap-kotlin",
  ":integration_tests:rap-ksp",
  ":integration_tests:roborazzi",
  ":integration_tests:room",
  ":integration_tests:sdkcompat",
  ":integration_tests:securityproviders",
  ":integration_tests:simulator:app",
  ":integration_tests:simulator:test",
  ":integration_tests:sparsearray",
  ":integration_tests:testparameterinjector",
  ":integration_tests:versioning",
  ":junit",
  ":nativeruntime",
  ":pluginapi",
  ":plugins:maven-dependency-resolver",
  ":preinstrumented",
  ":processor",
  ":processor-ksp",
  ":resources",
  ":robolectric",
  ":sandbox",
  ":shadowapi",
  ":shadows:framework",
  ":simulator",
  ":simulator-gradle-plugin",
  ":testapp",
  ":utils",
  ":utils:reflector",
)
