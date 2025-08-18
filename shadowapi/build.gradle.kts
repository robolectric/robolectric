plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  api(project(":annotations"))
  api(project(":utils"))
  testImplementation(libs.findbugs.jsr305)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
