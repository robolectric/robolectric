import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
  alias(libs.plugins.detekt)
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.spotless)
}

tasks.withType<GenerateModuleMetadata>().configureEach {
  // We don't want to release Gradle module metadata now to avoid
  // potential compatibility problems.
  enabled = false
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  api(project(":pluginapi"))
  api(project(":utils"))
  api(libs.auto.value.annotations)
  api(libs.guava)
  annotationProcessor(libs.auto.value)
  compileOnly(libs.findbugs.jsr305)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.kotlin.stdlib)
}
