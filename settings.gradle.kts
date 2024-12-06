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
  ":annotations",
  ":errorprone",
  ":integration_tests:androidx",
  ":integration_tests:androidx_test",
  ":integration_tests:composeui",
  ":integration_tests:ctesque",
  ":integration_tests:dependency-on-stubs",
  ":integration_tests:jacoco-offline",
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
  ":integration_tests:roborazzi",
  ":integration_tests:room",
  ":integration_tests:sdkcompat",
  ":integration_tests:security-providers",
  ":integration_tests:sparsearray",
  ":integration_tests:testparameterinjector",
  ":integration_tests:versioning",
  ":junit",
  ":nativeruntime",
  ":pluginapi",
  ":plugins:maven-dependency-resolver",
  ":preinstrumented",
  ":processor",
  ":resources",
  ":robolectric",
  ":sandbox",
  ":shadowapi",
  ":shadows:framework",
  ":shadows:httpclient",
  ":shadows:playservices",
  ":testapp",
  ":utils",
  ":utils:reflector",
)
