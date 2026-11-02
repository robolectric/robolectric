package org.robolectric.junit.jupiter

import kotlin.reflect.KClass
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory

/**
 * Marks a test class that only runs when a test launches it with [Fixtures.run], for example
 * because its tests are meant to fail.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EnabledIfSystemProperty(named = Fixtures.PROPERTY, matches = "true")
annotation class Fixture

/** Runs [Fixture] classes on the Jupiter engine, and records what the tests of a run do. */
object Fixtures {
  const val PROPERTY = "org.robolectric.junit.jupiter.fixtures"
  private const val EVENTS = "org.robolectric.junit.jupiter.fixtures.events"
  private val BUILD_PROPERTIES =
    listOf("robolectric.enabledSdks", "robolectric.alwaysIncludeVariantMarkersInTestName")

  /** The outcome of each test of a run, by its display name. */
  class Results(
    val succeeded: List<String>,
    val failed: Map<String, Throwable>,
    val aborted: List<String>,
    val skipped: Map<String, String>,
    /** The display names of the containers of the run, such as classes and groups of tests. */
    val containers: List<String>,
    /** What the tests recorded with [record], in order. */
    val events: List<String>,
  )

  /**
   * Records an event of a fixture. Test classes are loaded again in the Android environment, so
   * their static state isn't visible to the test that launches them, but system properties are.
   */
  fun record(event: String) {
    synchronized(System.getProperties()) {
      System.setProperty(EVENTS, System.getProperty(EVENTS, "") + event + "\n")
    }
  }

  /**
   * Runs the fixtures with the given system properties set, and the given configuration parameters
   * of JUnit. The properties that the build uses to select and name the SDKs of tests don't apply
   * to the run unless they are given, so that it doesn't depend on them.
   */
  fun run(
    vararg fixtures: KClass<*>,
    properties: Map<String, String> = emptyMap(),
    configuration: Map<String, String> = emptyMap(),
  ): Results {
    val succeeded = mutableListOf<String>()
    val failed = mutableMapOf<String, Throwable>()
    val aborted = mutableListOf<String>()
    val skipped = mutableMapOf<String, String>()
    val containers = mutableListOf<String>()
    val listener =
      object : TestExecutionListener {
        override fun executionFinished(identifier: TestIdentifier, result: TestExecutionResult) {
          if (identifier.isTest) {
            when (result.status!!) {
              TestExecutionResult.Status.SUCCESSFUL -> succeeded.add(identifier.displayName)
              TestExecutionResult.Status.FAILED ->
                failed[identifier.displayName] = result.throwable.get()
              TestExecutionResult.Status.ABORTED -> aborted.add(identifier.displayName)
            }
          } else {
            containers.add(identifier.displayName)
            if (result.status == TestExecutionResult.Status.FAILED) {
              failed[identifier.displayName] = result.throwable.get()
            }
          }
        }

        override fun executionSkipped(identifier: TestIdentifier, reason: String) {
          skipped[identifier.displayName] = reason
        }
      }
    val request =
      LauncherDiscoveryRequestBuilder.request()
        .selectors(fixtures.map { DiscoverySelectors.selectClass(it.java) })
        .filters(EngineFilter.includeEngines("junit-jupiter"))
        .configurationParameters(configuration)
        .build()
    val allProperties = properties + (PROPERTY to "true")
    val priorValues =
      (allProperties.keys + BUILD_PROPERTIES).associateWith { System.getProperty(it) }
    System.clearProperty(EVENTS)
    BUILD_PROPERTIES.forEach { System.clearProperty(it) }
    allProperties.forEach { (key, value) -> System.setProperty(key, value) }
    try {
      LauncherFactory.create().execute(request, listener)
    } finally {
      priorValues.forEach { (key, value) ->
        if (value == null) System.clearProperty(key) else System.setProperty(key, value)
      }
    }
    val events = System.getProperty(EVENTS, "").lines().filter { it.isNotEmpty() }
    System.clearProperty(EVENTS)
    return Results(succeeded, failed, aborted, skipped, containers, events)
  }
}
