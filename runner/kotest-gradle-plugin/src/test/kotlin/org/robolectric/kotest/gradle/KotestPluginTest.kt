package org.robolectric.kotest.gradle

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Tests the plugin in a build whose main classes stand in for Robolectric's Kotest extension, which
 * writes the guarded spec classes, and whose build logic stands in for Kotest's plugin, with its
 * task that runs Kotest's launcher.
 */
class KotestPluginTest {
  @TempDir lateinit var project: File

  @Test
  fun `the guarded spec classes are before the class path of a task of Kotest's plugin`() {
    val output = run("jvmKotest")

    assertTrue("jvmKotest sees guarded classes first: true" in output, output)
  }

  @Test
  fun `a task that isn't one of Kotest's plugin is left alone`() {
    val output = run("other")

    assertTrue("other sees guarded classes first: false" in output, output)
  }

  private fun run(task: String): String {
    write("settings.gradle.kts", """rootProject.name = "fixture"""")
    write(
      "buildSrc/src/main/java/io/kotest/framework/gradle/tasks/KotestJvmTask.java",
      """
      package io.kotest.framework.gradle.tasks;

      public abstract class KotestJvmTask extends org.gradle.api.tasks.JavaExec {}
      """,
    )
    write(
      "src/main/java/org/robolectric/kotest/internal/SpecClassWriter.java",
      """
      package org.robolectric.kotest.internal;

      import java.nio.file.Files;
      import java.nio.file.Paths;

      public class SpecClassWriter {
        public static void main(String[] arguments) throws Exception {
          Files.createDirectories(Paths.get(arguments[0]));
          Files.write(Paths.get(arguments[0], "guarded.txt"), new byte[0]);
        }
      }
      """,
    )
    write(
      "src/main/java/Main.java",
      """
      import java.io.File;

      public class Main {
        public static void main(String[] arguments) {
          String first = System.getProperty("java.class.path").split(File.pathSeparator)[0];
          boolean guarded = new File(first, "guarded.txt").isFile();
          System.out.println(arguments[0] + " sees guarded classes first: " + guarded);
        }
      }
      """,
    )
    write(
      "build.gradle.kts",
      """
      import io.kotest.framework.gradle.tasks.KotestJvmTask

      plugins {
        java
        id("org.robolectric.kotest")
      }

      tasks.register<KotestJvmTask>("jvmKotest") {
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass.set("Main")
        args("jvmKotest")
      }

      tasks.register<JavaExec>("other") {
        classpath = sourceSets.main.get().runtimeClasspath
        mainClass.set("Main")
        args("other")
      }
      """,
    )
    return GradleRunner.create()
      .withProjectDir(project)
      .withPluginClasspath()
      .withArguments(task, "--configuration-cache", "--stacktrace")
      .build()
      .output
  }

  private fun write(path: String, content: String) {
    val file = File(project, path)
    file.parentFile.mkdirs()
    file.writeText(content.trimIndent())
  }
}
