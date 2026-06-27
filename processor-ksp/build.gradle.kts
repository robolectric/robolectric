import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.detekt)
  alias(libs.plugins.robolectric.deployed.kotlin.module)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.spotless)
  kotlin("jvm")
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  compileOnly(libs.ksp.api)
  implementation(libs.kotlin.stdlib)

  // shadowapi provides the ShadowProvider/ClassTracker/Shadow types that the generated code (which
  // the compile tests compile via kctfork) references.
  testImplementation(project(":shadowapi"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.kctfork.core)
  testImplementation(libs.kctfork.ksp)
}

tasks.withType<Test> {
  // kctfork compiles sources with an in-process javac; opening these packages is required.
  jvmArgs(
    "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
  )
}
