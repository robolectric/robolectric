import org.gradle.internal.jvm.Jvm

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

// The `errorprone` library now needs some additional exports to allow it to
// perform reflections. The flags passed to the JVM are from the errorprone
// documentation: https://errorprone.info/docs/installation.
tasks.compileJava.configure {
  options.compilerArgs.addAll(
    listOf(
      "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
      "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
    )
  )
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
  val toolsJar: File? = Jvm.current().getToolsJar()
  toolsJar?.let { "compile"(files(it)) }

  // Testing dependencies
  testImplementation(libs.junit4)
  testImplementation(libs.error.prone.test.helpers)
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
}
