plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  api(project(":utils"))
  api(project(":pluginapi"))

  api(libs.auto.value.annotations)
  api(libs.guava)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)

  annotationProcessor(libs.auto.value)
}
