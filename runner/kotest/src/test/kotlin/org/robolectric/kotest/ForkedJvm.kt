package org.robolectric.kotest

import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/**
 * Runs a main class in a JVM of its own, which has the class path of this one: for what a launcher
 * does before anything of Kotest or of the JUnit Platform can be hooked into.
 */
object ForkedJvm {
  /** Kotest's own launcher, which initializes the spec classes before anything else. */
  const val KOTEST = "io.kotest.engine.launcher.MainKt"

  class Result(val exitValue: Int, val output: String)

  /**
   * @param withAgent whether the JVM is started with the module as a Java agent
   * @param before a directory to put before the class path
   */
  fun run(
    mainClass: String,
    vararg arguments: String,
    withAgent: Boolean = false,
    before: Path? = null,
  ): Result {
    val java = Paths.get(System.getProperty("java.home"), "bin", "java").toString()
    // What the build lets the JVM of the tests open to Robolectric.
    val opened =
      ManagementFactory.getRuntimeMXBean().inputArguments.filter {
        it.startsWith("--add-") || it.startsWith("--enable-")
      }
    val agent = "-javaagent:" + System.getProperty("org.robolectric.kotest.agent")
    val classPath = listOfNotNull(before?.toString(), System.getProperty("java.class.path"))
    val command =
      listOf(java) +
        opened +
        listOfNotNull(agent.takeIf { withAgent }) +
        listOf("-D${Fixtures.PROPERTY}=true", "-cp", classPath.joinToString(File.pathSeparator)) +
        mainClass +
        arguments
    val process = ProcessBuilder(command).redirectErrorStream(true).start()
    val output = process.inputStream.bufferedReader().use { it.readText() }
    check(process.waitFor(1, TimeUnit.MINUTES)) { "The JVM didn't exit:\n$output" }
    return Result(process.exitValue(), output)
  }
}
