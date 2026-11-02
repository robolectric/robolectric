import org.robolectric.gradle.AndroidSdk

plugins {
  alias(libs.plugins.robolectric.deployed.kotlin.module)
  alias(libs.plugins.robolectric.kotlin.module)
  alias(libs.plugins.detekt)
}

kotlin {
  // Nothing is part of the API unless it is declared public.
  explicitApi()
}

// The jar is a Java agent too, for the launchers that the module can't hook into.
tasks.jar {
  manifest { attributes("Premain-Class" to "org.robolectric.kotest.internal.SpecClassAgent") }
}

tasks.withType<Test> {
  useJUnitPlatform { includeEngines("kotest") }

  // The tests of the Java agent start JVMs with it.
  val agent = tasks.jar.flatMap { it.archiveFile }
  inputs.file(agent)
  jvmArgumentProviders.add(
    CommandLineArgumentProvider { listOf("-Dorg.robolectric.kotest.agent=${agent.get().asFile}") }
  )
}

dependencies {
  api(libs.kotest.framework.engine)
  api(project(":robolectric"))
  implementation(project(":runner:common"))
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.test)
  implementation(libs.asm)
  // For the tree API of ASM.
  implementation(libs.asm.commons)
  // For the listener of launcher sessions, which only the JUnit Platform loads.
  compileOnly(platform(libs.junit.jupiter.bom))
  compileOnly(libs.junit.platform.launcher)

  testImplementation(platform(libs.junit.jupiter.bom))
  testImplementation(libs.junit.platform.launcher)
  testImplementation(libs.kotest.runner.junit6)
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.kotlinx.coroutines.android)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(androidStubsJar())
  testRuntimeOnly(AndroidSdk.MAX_SDK.preinstrumentedCoordinates)
}

fun androidStubsJar(): ConfigurableFileCollection {
  val androidStubsVersion = libs.versions.androidstubs.get()
  val androidHome = System.getenv("ANDROID_HOME")
  if (androidHome.isNullOrBlank()) {
    throw GradleException("ANDROID_HOME environment variable not set or blank.")
  }
  return files("$androidHome/platforms/android-$androidStubsVersion/android.jar")
}
