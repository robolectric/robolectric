plugins { alias(libs.plugins.robolectric.java.module) }

val earlyRuntime by configurations.registering
val axtJunitVersion: String by rootProject.extra

dependencies {
  // compile against latest Android SDK (AndroidSdk.s.coordinates) { force = true }
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(project(":robolectric"))
  testImplementation(libs.truth)
  testImplementation("androidx.test.ext:junit:$axtJunitVersion@aar")
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates) // compile against latest Android SDK
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates) // run against whatever this JDK supports
}
