plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  api(project(":sandbox"))
  api(project(":pluginapi"))

  compileOnly(libs.junit4)
}
