import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.spotless)
}

tasks.named<KotlinCompile>("compileKotlin").configure {
  compilerOptions.jvmTarget = JvmTarget.JVM_1_8
}

tasks.named<KotlinCompile>("compileTestKotlin").configure {
  compilerOptions.jvmTarget = JvmTarget.JVM_1_8
}

dependencies {
  api(project(":robolectric"))
  compileOnly(AndroidSdk.MAX_SDK.coordinates)

  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockk)
}
