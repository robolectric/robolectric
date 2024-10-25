plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

val axtCoreVersion: String by rootProject.extra
val axtJunitVersion: String by rootProject.extra
val axtMonitorVersion: String by rootProject.extra
val axtRunnerVersion: String by rootProject.extra
val axtTruthVersion: String by rootProject.extra
val espressoVersion: String by rootProject.extra

dependencies {
  annotationProcessor(libs.auto.service)
  annotationProcessor(libs.error.prone.core)

  api(project(":annotations"))
  api(project(":junit"))
  api(project(":pluginapi"))
  api(project(":resources"))
  api(project(":sandbox"))
  api(project(":utils"))
  api(project(":utils:reflector"))
  api(project(":plugins:maven-dependency-resolver"))
  api(libs.javax.inject)
  compileOnly(libs.auto.service.annotations)
  api(libs.javax.annotation.api)

  // We need to have shadows-framework.jar on the runtime system classpath so ServiceLoader
  //   can find its META-INF/services/org.robolectric.shadows.ShadowAdapter.
  api(project(":shadows:framework"))

  implementation(libs.conscrypt.openjdk.uber)
  implementation(libs.snakeyaml)

  api(libs.bcprov.jdk18on)
  compileOnly(libs.findbugs.jsr305)

  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  compileOnly(libs.junit4)
  compileOnly(libs.androidx.annotation)

  api("androidx.test:monitor:$axtMonitorVersion@aar")
  implementation("androidx.test.espresso:espresso-idling-resource:$espressoVersion@aar")
  implementation("com.google.testparameterinjector:test-parameter-injector:1.18@jar")

  testImplementation(libs.androidx.annotation)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)
  testImplementation(libs.hamcrest.junit)
  testImplementation("androidx.test:core:$axtCoreVersion@aar")
  testImplementation("androidx.test.ext:junit:$axtJunitVersion@aar")
  testImplementation("androidx.test.ext:truth:$axtTruthVersion@aar")
  testImplementation("androidx.test:runner:$axtRunnerVersion@aar")
  testImplementation(libs.guava)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates) // compile against latest Android SDK
  testRuntimeOnly(androidStubsJar())
}

fun androidStubsJar(): ConfigurableFileCollection {
  val androidStubsVersion = libs.versions.androidstubs.get()
  if ("ANDROID_HOME" !in System.getenv()) {
    throw GradleException("Environment variable ANDROID_HOME is not set.")
  }

  val androidJarPath =
    "${System.getenv("ANDROID_HOME")}/platforms/android-$androidStubsVersion/android.jar"
  val androidJar = file(androidJarPath)
  if (!androidJar.exists()) {
    throw GradleException(
      "android.jar $androidStubsVersion not found at $androidJarPath. Download it through Android SDK Manager, or install the `Android SDK Command-line tools` and run `sdkmanager \"platforms;android-$androidStubsVersion\"`."
    )
  }

  return files(androidJarPath)
}
