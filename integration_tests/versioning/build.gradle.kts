plugins { alias(libs.plugins.robolectric.java.module) }

dependencies {
  // compile against latest Android SDK (AndroidSdk.s.coordinates) { force = true }
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(project(":robolectric"))
  testImplementation(libs.truth)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates) // compile against latest Android SDK
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates) // run against whatever this JDK supports
}
