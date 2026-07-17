import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.robolectric.gradle.AndroidSdk

plugins {
  alias(libs.plugins.jacoco)
  alias(libs.plugins.robolectric.java.module)
}

val jacocoVersion = libs.versions.jacoco.get()
val jacocoAnt = configurations.named("jacocoAnt")
val jacocoRuntime = configurations.register("jacocoRuntime")

jacoco { toolVersion = jacocoVersion }

dependencies {
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)

  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation("org.jacoco:org.jacoco.agent:$jacocoVersion:runtime")
}

val unitTestTaskName = "test"
val jacocoInstrumentedClassesOutputDir =
  layout.buildDirectory.dir("$jacocoVersion/classes/java/classes-instrumented")

val jacocoInstrument =
  tasks.register<JacocoInstrumentTask>("jacocoInstrument") {
    inputDirs.from(sourceSets.named("main").map { it.output.classesDirs })
    jacocoAntClasspath.from(jacocoAnt)
    outputDir.set(jacocoInstrumentedClassesOutputDir)
  }

// Ensure jacocoInstrument runs when classes is called
tasks.classes { finalizedBy(jacocoInstrument) }

@CacheableTask
abstract class JacocoInstrumentTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val inputDirs: ConfigurableFileCollection

  @get:Classpath abstract val jacocoAntClasspath: ConfigurableFileCollection

  @get:OutputDirectory abstract val outputDir: DirectoryProperty

  @TaskAction
  fun instrument() {
    logger.debug("[JaCoCo] Generating JaCoCo instrumented classes for the build.")

    val outputDirPath = outputDir.get().asFile.path

    ant.withGroovyBuilder {
      "taskdef"(
        "name" to "instrument",
        "classname" to "org.jacoco.ant.InstrumentTask",
        "classpath" to jacocoAntClasspath.asPath,
      )
    }

    inputDirs.files.forEach { inputDir ->
      if (inputDir.exists()) {
        ant.withGroovyBuilder {
          "instrument"("destdir" to outputDirPath) {
            "fileset"("dir" to inputDir.path, "excludes" to "")
          }
        }
      } else {
        logger.debug("[JaCoCo] Classes directory with path '{}' does not exist.", inputDir)
      }
    }
  }
}

val executionDataFilePath = layout.buildDirectory.file("jacoco/${unitTestTaskName}.exec")

// Put JaCoCo instrumented classes and JaCoCoRuntime at the beginning of the JVM classpath.
tasks.named<Test>(unitTestTaskName).configure {
  // Disable JaCoCo on-the-fly from Gradle JaCoCo plugin.
  extensions.configure<JacocoTaskExtension> { isEnabled = false }

  logger.debug("[JaCoCo] Modifying classpath of tests JVM.")

  systemProperty("jacoco-agent.destfile", executionDataFilePath.map { it.asFile.path })

  // Use the output of jacocoInstrument task.
  // We prepend it to classpath so instrumented classes are loaded first.
  classpath = files(jacocoInstrument.map { it.outputDir }, classpath, jacocoRuntime)

  doFirst { logger.debug("[JaCoCo] Final test JVM classpath is ${classpath.asPath}") }
}
