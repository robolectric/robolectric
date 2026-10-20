package org.robolectric.kotest

import io.kotest.core.annotation.Condition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import kotlin.reflect.KClass
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.robolectric.kotest.internal.Sessions

/**
 * Marks a spec that only runs when a test launches it with [Fixtures.run], for example because its
 * tests are meant to fail.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EnabledIf(RunsAsFixture::class)
annotation class Fixture

class RunsAsFixture : Condition {
  override fun evaluate(kclass: KClass<out Spec>): Boolean =
    System.getProperty(Fixtures.PROPERTY) == "true"
}

/** Runs [Fixture] specs on the Kotest engine, and records what the tests of a run do. */
object Fixtures {
  const val PROPERTY = "org.robolectric.kotest.fixtures"
  private const val EVENTS = "org.robolectric.kotest.fixtures.events"

  // What the build sets for all tests: which SDKs they run on, and how they are named.
  private val BUILD_PROPERTIES =
    listOf("robolectric.enabledSdks", "robolectric.alwaysIncludeVariantMarkersInTestName")

  /** The outcome of each test and each spec of a run, by its display name. */
  class Results(
    val succeeded: List<String>,
    val failed: Map<String, Throwable>,
    val skipped: Map<String, String>,
    /** What the tests recorded with [record], in order. */
    val events: List<String>,
  )

  /**
   * Records an event of a fixture. Specs are loaded again in the Android environment, so their
   * static state isn't visible to the test that launches them, but system properties are.
   */
  fun record(event: String) {
    synchronized(System.getProperties()) {
      System.setProperty(EVENTS, System.getProperty(EVENTS, "") + event + "\n")
    }
  }

  /**
   * Launches fixtures with the given system properties set, in a session of their own. The
   * properties that the build sets for all tests don't apply unless they are given, so that the
   * launch doesn't depend on them. The test that calls this can't be one that runs in an Android
   * environment itself.
   *
   * @param closesSession whether to close the session if Kotest left it open
   */
  fun launch(
    properties: Map<String, String> = emptyMap(),
    closesSession: Boolean = true,
    launcher: () -> Unit,
  ) {
    val allProperties = properties + (PROPERTY to "true")
    val priorValues =
      (allProperties.keys + BUILD_PROPERTIES).associateWith { System.getProperty(it) }
    System.clearProperty(EVENTS)
    BUILD_PROPERTIES.forEach { System.clearProperty(it) }
    allProperties.forEach { (key, value) -> System.setProperty(key, value) }
    // The fixtures get a session of their own, which reads the properties.
    Sessions.closeIfIdle()
    try {
      launcher()
    } finally {
      if (closesSession) {
        Sessions.closeIfIdle()
      }
      priorValues.forEach { (key, value) ->
        if (value == null) System.clearProperty(key) else System.setProperty(key, value)
      }
    }
  }

  /**
   * Runs the fixtures on Kotest's runner for the JUnit Platform, see [launch].
   *
   * @param parameters the configuration parameters of the JUnit Platform for the run
   */
  fun run(
    vararg fixtures: KClass<*>,
    properties: Map<String, String> = emptyMap(),
    parameters: Map<String, String> = emptyMap(),
    closesSession: Boolean = true,
  ): Results {
    val succeeded = mutableListOf<String>()
    val failed = mutableMapOf<String, Throwable>()
    val skipped = mutableMapOf<String, String>()
    val listener =
      object : TestExecutionListener {
        override fun executionFinished(identifier: TestIdentifier, result: TestExecutionResult) {
          when (result.status!!) {
            TestExecutionResult.Status.SUCCESSFUL ->
              if (identifier.isTest) succeeded.add(identifier.displayName)
            TestExecutionResult.Status.FAILED ->
              failed[identifier.displayName] = result.throwable.get()
            TestExecutionResult.Status.ABORTED -> skipped[identifier.displayName] = ""
          }
        }

        override fun executionSkipped(identifier: TestIdentifier, reason: String) {
          skipped[identifier.displayName] = reason
        }
      }
    val request =
      LauncherDiscoveryRequestBuilder.request()
        .selectors(fixtures.map { DiscoverySelectors.selectClass(it.java) })
        .filters(EngineFilter.includeEngines("kotest"))
        .configurationParameters(parameters)
        .build()
    launch(properties, closesSession) { LauncherFactory.create().execute(request, listener) }
    val events = System.getProperty(EVENTS, "").lines().filter { it.isNotEmpty() }
    System.clearProperty(EVENTS)
    return Results(succeeded, failed, skipped, events)
  }
}
