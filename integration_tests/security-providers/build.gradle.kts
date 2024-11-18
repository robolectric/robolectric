plugins { alias(libs.plugins.robolectric.java.module) }

dependencies {
  api(project(":robolectric"))
  compileOnly(AndroidSdk.MAX_SDK.coordinates)

  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(libs.conscrypt.openjdk.uber)
  testImplementation(libs.junit4)
  testImplementation(libs.okhttp)
  testImplementation(platform(libs.okhttp.bom))
}
