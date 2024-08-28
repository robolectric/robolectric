plugins { alias(libs.plugins.robolectric.java.module) }

dependencies {
  api(project(":robolectric"))
  compileOnly(AndroidSdk.MAX_SDK.coordinates)

  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.bundles.powermock)
}
