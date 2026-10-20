package org.robolectric.junit.jupiter.internal

import java.util.Collections
import java.util.IdentityHashMap
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment
import org.robolectric.runner.common.RobolectricSession

/**
 * The Android environments a test or a class runs in, one for each of its SDKs. A test shares them
 * with its class, or owns them, in which case closing this closes them.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class TestEnvironments(
  val environments: List<RobolectricEnvironment>,
  val shared: Boolean,
) : AutoCloseable {
  /** Those in which an assumption of the test failed, where the rest of the test is skipped. */
  val aborted: MutableSet<RobolectricEnvironment> = Collections.newSetFromMap(IdentityHashMap())

  // What to do in each environment in which the test started, once the test is over.
  private val testEndActions = IdentityHashMap<RobolectricEnvironment, () -> Unit>()

  /** Returns whether the test was started in the environment, by [onTestEnd]. */
  fun hasStartedIn(environment: RobolectricEnvironment): Boolean =
    testEndActions.containsKey(environment)

  /** Starts the test in the environment, in which the action runs when the test is over. */
  fun onTestEnd(environment: RobolectricEnvironment, action: () -> Unit) {
    testEndActions[environment] = action
  }

  override fun close() {
    val failures =
      testEndActions.mapNotNull { (environment, action) ->
        runCatching { environment.run { action() } }.exceptionOrNull()
      } +
        environments.mapNotNull { environment ->
          if (shared) null else runCatching { environment.close() }.exceptionOrNull()
        }
    failures.drop(1).forEach { failures.first().addSuppressed(it) }
    failures.firstOrNull()?.let { throw it }
  }

  companion object {
    /** Opens an environment for each configuration, which the returned object owns. */
    fun open(session: RobolectricSession, configurations: List<Configuration>): TestEnvironments {
      val environments = mutableListOf<RobolectricEnvironment>()
      try {
        configurations.forEach { environments.add(session.open(it)) }
      } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        environments.forEach { it.close() }
        throw e
      }
      return TestEnvironments(environments, shared = false)
    }
  }
}
