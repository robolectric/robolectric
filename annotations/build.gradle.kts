plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  compileOnly(libs.javax.annotation.api)
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(libs.truth)
  testImplementation(libs.junit4)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates) // compile against latest Android SDK
}
