plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

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

  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  compileOnly(libs.junit4)
  compileOnly(libs.androidx.annotation)

  api(variantOf(libs.androidx.test.monitor) { artifactType("aar") })
  implementation(variantOf(libs.androidx.test.espresso.idling.resource) { artifactType("aar") })
  implementation(variantOf(libs.test.parameter.injector) { artifactType("jar") })

  testImplementation(libs.androidx.annotation)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)
  testImplementation(libs.mockito.subclass)
  testImplementation(libs.hamcrest)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testImplementation(variantOf(libs.androidx.test.ext.junit) { artifactType("aar") })
  testImplementation(variantOf(libs.androidx.test.ext.truth) { artifactType("aar") })
  testImplementation(variantOf(libs.androidx.test.runner) { artifactType("aar") })
  testImplementation(libs.guava)
  testImplementation(libs.guava.testlib)
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
