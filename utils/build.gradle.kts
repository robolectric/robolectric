import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

tasks.compileKotlin.configure { compilerOptions.jvmTarget = JvmTarget.JVM_1_8 }

tasks.compileTestKotlin.configure { compilerOptions.jvmTarget = JvmTarget.JVM_1_8 }

dependencies {
  api(project(":annotations"))
  api(project(":pluginapi"))
  api(libs.javax.inject)
  api(libs.javax.annotation.api)

  compileOnly(libs.findbugs.jsr305)

  testCompileOnly(libs.auto.service.annotations)
  testAnnotationProcessor(libs.auto.service)
  testAnnotationProcessor(libs.error.prone.core)
  implementation(libs.error.prone.annotations)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.kotlin.stdlib)
}
