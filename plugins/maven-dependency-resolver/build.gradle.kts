import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.spotless)
}

tasks.withType<GenerateModuleMetadata>().configureEach {
  // We don't want to release Gradle module metadata now to avoid
  // potential compatibility problems.
  enabled = false
}

tasks.named<KotlinCompile>("compileKotlin") { compilerOptions.jvmTarget = JvmTarget.JVM_1_8 }

tasks.named<KotlinCompile>("compileTestKotlin") { compilerOptions.jvmTarget = JvmTarget.JVM_1_8 }

dependencies {
  api(project(":pluginapi"))
  api(project(":utils"))
  api(libs.auto.value.annotations)
  api(libs.guava)
  annotationProcessor(libs.auto.value)

  testImplementation(libs.junit4)
  testImplementation(libs.mockito)
  testImplementation(libs.truth)
  testImplementation(libs.kotlin.stdlib)
}
