plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  implementation(project(":pluginapi"))
  api(project(":sandbox"))
  implementation(project(":shadows:framework"))

  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  compileOnly(libs.findbugs.jsr305)
  compileOnly(libs.junit4)

  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(variantOf(libs.androidx.test.ext.junit) { artifactType("aar") })
  testImplementation(variantOf(libs.androidx.test.runner) { artifactType("aar") })
  testImplementation(libs.hamcrest)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
