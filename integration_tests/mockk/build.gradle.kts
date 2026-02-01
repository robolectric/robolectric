import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  alias(libs.plugins.detekt)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.spotless)
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  api(project(":robolectric"))
  compileOnly(AndroidSdk.MAX_SDK.coordinates)

  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockk)
}
