plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.shadows)
}

shadows {
  packageName = "org.robolectric.shadows.httpclient"
  sdkCheckMode = "OFF"
}

val earlyRuntime = configurations.create("earlyRuntime")
val axtJunitVersion: String by rootProject.extra

dependencies {
  api(project(":annotations"))
  api(project(":shadowapi"))
  api(project(":utils"))

  // We should keep httpclient version for low level API compatibility.
  earlyRuntime(libs.apache.http.core)
  api(libs.apache.http.client)
  compileOnly(AndroidSdk.LOLLIPOP_MR1.coordinates)

  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)
  testImplementation("androidx.test.ext:junit:$axtJunitVersion@aar")

  testCompileOnly(AndroidSdk.LOLLIPOP_MR1.coordinates)
  testRuntimeOnly(AndroidSdk.S.coordinates)
}

// httpcore needs to come before android-all on runtime classpath; the gradle IntelliJ plugin
//   needs the compileClasspath order patched too (bug?)
val mainSourceSet = sourceSets.getByName("main")

mainSourceSet.compileClasspath = earlyRuntime + mainSourceSet.compileClasspath

mainSourceSet.runtimeClasspath = earlyRuntime + mainSourceSet.runtimeClasspath

val testSourceSet = sourceSets.getByName("test")

testSourceSet.compileClasspath = earlyRuntime + testSourceSet.compileClasspath

testSourceSet.runtimeClasspath = earlyRuntime + testSourceSet.runtimeClasspath
