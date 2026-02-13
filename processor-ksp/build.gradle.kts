import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.detekt)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.spotless)
  kotlin("jvm")
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  implementation(libs.ksp.api)
  implementation(libs.kotlin.stdlib)
}
