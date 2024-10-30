import org.gradle.internal.jvm.Jvm

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

// Disable annotation processor for tests
tasks.compileTestJava.configure { options.compilerArgs.add("-proc:none") }

tasks.test.configure { enabled = false }

dependencies {
  // Project dependencies
  implementation(project(":annotations"))
  implementation(project(":shadowapi"))

  // Compile dependencies
  implementation(libs.error.prone.annotations)
  implementation(libs.error.prone.refaster)
  implementation(libs.error.prone.check.api)
  compileOnly(libs.auto.service.annotations)
  compileOnly(AndroidSdk.MAX_SDK.coordinates)

  annotationProcessor(libs.auto.service)
  annotationProcessor(libs.error.prone.core)

  // In JDK 9, tools.jar disappears!
  val toolsJar = Jvm.current().getToolsJar()
  if (toolsJar != null) {
    "compile"(files(toolsJar))
  }

  // Testing dependencies
  testImplementation(libs.junit4)
  testImplementation(libs.error.prone.test.helpers)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
}
