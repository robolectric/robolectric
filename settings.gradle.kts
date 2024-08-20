pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "robolectric"

include(
  ":robolectric",
  ":sandbox",
  ":junit",
  ":utils",
  ":utils:reflector",
  ":pluginapi",
  ":plugins:maven-dependency-resolver",
  ":preinstrumented",
  ":processor",
  ":resources",
  ":annotations",
  ":shadows:framework",
  ":shadows:httpclient",
  ":shadows:multidex",
  ":shadows:playservices",
  ":shadowapi",
  ":errorprone",
  ":nativeruntime",
  ":integration_tests:agp",
  ":integration_tests:agp:testsupport",
  ":integration_tests:dependency-on-stubs",
  ":integration_tests:kotlin",
  ":integration_tests:libphonenumber",
  ":integration_tests:memoryleaks",
  ":integration_tests:mockito",
  ":integration_tests:mockito-kotlin",
  ":integration_tests:mockito-experimental",
  ":integration_tests:powermock",
  ":integration_tests:roborazzi",
  ":integration_tests:androidx",
  ":integration_tests:androidx_test",
  ":integration_tests:ctesque",
  ":integration_tests:security-providers",
  ":integration_tests:mockk",
  ":integration_tests:jacoco-offline",
  ":integration_tests:sdkcompat",
  ":integration_tests:multidex",
  ":integration_tests:play_services",
  ":integration_tests:sparsearray",
  ":integration_tests:nativegraphics",
  ":integration_tests:room",
  ":integration_tests:versioning",
  ":testapp",
)
