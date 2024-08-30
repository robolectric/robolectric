plugins {
  alias(libs.plugins.jacoco)
  alias(libs.plugins.robolectric.java.module)
}

val jacocoVersion = libs.versions.jacoco.get()
val jacocoAnt: Configuration by configurations.getting
val jacocoRuntime: Configuration by configurations.creating

jacoco { toolVersion = jacocoVersion }

dependencies {
  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)

  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation("org.jacoco:org.jacoco.agent:$jacocoVersion:runtime")
}

val unitTestTaskName = "test"
val javaDir = layout.buildDirectory.dir("classes/java/main").get().asFile
val kotlinDir = layout.buildDirectory.dir("classes/kotlin/main").get().asFile
val jacocoInstrumentedClassesOutputDir =
  layout.buildDirectory.dir("$jacocoVersion/classes/java/classes-instrumented").get().asFile

// Make sure it's evaluated after the AGP evaluation.
afterEvaluate {
  tasks.classes.configure {
    doLast {
      logger.debug("[JaCoCo] Generating JaCoCo instrumented classes for the build.")

      if (jacocoInstrumentedClassesOutputDir.exists()) {
        logger.debug("[JaCoCo] Classes had been instrumented.")
        return@doLast
      }

      ant.withGroovyBuilder {
        "taskdef"(
          "name" to "instrument",
          "classname" to "org.jacoco.ant.InstrumentTask",
          "classpath" to jacocoAnt.asPath,
        )
      }

      if (javaDir.exists()) {
        ant.withGroovyBuilder {
          "instrument"("destdir" to jacocoInstrumentedClassesOutputDir.path) {
            "fileset"("dir" to javaDir.path, "excludes" to "")
          }
        }
      } else {
        logger.debug("[JaCoCo] Classes directory with path '{}' does not exist.", javaDir)
      }

      if (kotlinDir.exists()) {
        ant.withGroovyBuilder {
          "instrument"("destdir" to jacocoInstrumentedClassesOutputDir.path) {
            "fileset"("dir" to kotlinDir.path, "excludes" to "")
          }
        }
      } else {
        logger.debug("[JaCoCo] Classes directory with path '{}' does not exist.", kotlinDir)
      }
    }
  }

  val executionDataFilePath =
    layout.buildDirectory.dir("jacoco").get().file("${unitTestTaskName}.exec").asFile.path

  // Put JaCoCo instrumented classes and JaCoCoRuntime at the beginning of the JVM classpath.
  tasks.named<Test>(unitTestTaskName).configure {
    doFirst {
      // Disable JaCoCo on-the-fly from Gradle JaCoCo plugin.
      extensions.configure<JacocoTaskExtension> { isEnabled = false }

      logger.debug("[JaCoCo] Modifying classpath of tests JVM.")

      systemProperty("jacoco-agent.destfile", executionDataFilePath)

      classpath = files(jacocoInstrumentedClassesOutputDir.path) + classpath + jacocoRuntime

      logger.debug("[JaCoCo] Final test JVM classpath is ${classpath.asPath}")
    }
  }
}
