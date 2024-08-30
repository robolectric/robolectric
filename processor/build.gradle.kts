import org.gradle.internal.jvm.Jvm

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

abstract class GenerateSdksFileTask : DefaultTask() {
  @get:OutputFile abstract var outFile: File

  @TaskAction
  @Throws(Exception::class)
  fun writeProperties() {
    val outDir = outFile.parentFile
    if (!outDir.isDirectory) {
      outDir.mkdirs()
    }

    outFile.printWriter().use { out ->
      out.write("# GENERATED by $this -- do not edit\n")

      AndroidSdk.ALL_SDKS.forEach { androidSdk ->
        val config = project.configurations.create("processor_sdk${androidSdk.apiLevel}")
        project.dependencies.add("processor_sdk${androidSdk.apiLevel}", androidSdk.coordinates)

        val sdkPath = config.files.first().absolutePath
        out.write("$sdkPath\n")
      }
    }
  }
}

val generateSdksFile by
  tasks.registering(GenerateSdksFileTask::class) {
    outFile = project.rootProject.layout.buildDirectory.file("sdks.txt").get().asFile
  }

tasks.classes.configure { dependsOn(generateSdksFile) }

dependencies {
  api(project(":annotations"))
  api(project(":shadowapi"))

  compileOnly(libs.findbugs.jsr305)
  api(libs.asm)
  api(libs.asm.commons)
  api(libs.asm.util)
  api(libs.guava)
  api(libs.gson)
  implementation(libs.auto.common)

  val toolsJar = Jvm.current().getToolsJar()
  if (toolsJar != null) {
    implementation(files(toolsJar))
  }

  testImplementation(libs.javax.annotation.jsr250.api)
  testImplementation(libs.junit4)
  testImplementation(libs.mockito)
  testImplementation(libs.compile.testing)
  testImplementation(libs.truth)
}
