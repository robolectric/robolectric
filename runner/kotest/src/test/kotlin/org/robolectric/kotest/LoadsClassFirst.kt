package org.robolectric.kotest

import kotlin.system.exitProcess

/**
 * Runs a fixture as a build tool does that loads a test class before it opens a launcher session of
 * the JUnit Platform, as Maven Surefire 3.5.2 does with the first test class that it finds. Exits
 * with 0 if the tests of the fixture succeed.
 */
fun main(arguments: Array<String>) {
  val loader = Thread.currentThread().contextClassLoader
  val fixture = Class.forName(arguments.single(), false, loader)
  val results = Fixtures.run(fixture.kotlin)
  println("succeeded: ${results.succeeded}, failed: ${results.failed.keys}")
  exitProcess(if (results.failed.isEmpty() && results.succeeded.isNotEmpty()) 0 else 1)
}
