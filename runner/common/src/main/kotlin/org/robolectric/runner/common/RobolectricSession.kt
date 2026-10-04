package org.robolectric.runner.common

import java.lang.reflect.Method
import java.util.Properties
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.internal.DefaultRobolectricSession

/**
 * The entry point for running code in Robolectric's Android environments. A test framework
 * integration creates one session for a test run, and closes it when the run ends.
 *
 * An integration first asks the session to [plan] where a test runs, which is cheap and has no side
 * effects, and then [open]s the environments it runs in. How long it keeps an environment open
 * decides what shares Android state: an environment opened for one test and closed after it
 * isolates the test, and one kept open for a test class lets the tests of the class share state.
 *
 * A tool that needs Android without a test framework, such as a REPL or a preview renderer, plans
 * its environment from a configuration that it builds, in place of that of a test:
 * ```
 * RobolectricSession.create().use { session ->
 *   val android = session.open(session.plan(Config.Builder().setSdk(34).build()).single())
 *   val application = android.run {
 *     android.classLoader.loadClass("org.robolectric.RuntimeEnvironment")
 *       .getMethod("getApplication").invoke(null)
 *   }
 * }
 * ```
 *
 * The caller's own classes can't use Android classes directly: code that does has to be loaded by
 * the environment's class loader, see [RobolectricEnvironment.loadClass].
 *
 * A session can be used from several threads, to run tests in parallel: every open environment has
 * a sandbox to itself.
 */
@ExperimentalRunnerApi
public interface RobolectricSession : AutoCloseable {
  /**
   * Returns the configurations that a test runs with: one for each Android SDK that Robolectric
   * selects for it, lowest first, given its configuration such as [Config] and the
   * `robolectric.enabledSdks` property. The list is empty if no SDK is selected.
   *
   * Each is the configuration of the test as Robolectric resolves it, its [Config] and its modes,
   * with that one SDK as the only one of the [Config]: `get(Config::class.java).sdk`. Two of them
   * are equal if their environments are interchangeable, so an integration can let tests with equal
   * configurations share an environment.
   *
   * An inner class is configured like the class that encloses it, unless it is configured itself.
   *
   * @param testClass the class the test belongs to
   * @param testMethod the test's method, or null for an environment shared by the whole class,
   *   which only the configuration of the class applies to
   * @throws IllegalArgumentException if the test's configuration is invalid
   */
  public fun plan(testClass: Class<*>, testMethod: Method?): List<Configuration>

  /**
   * Returns the configurations for a [Config] that isn't that of a test, as [Config.Builder] builds
   * it, on top of Robolectric's default configuration: one for each Android SDK that it selects,
   * lowest first, as for a test. Without an SDK, that is what Robolectric selects for a test that
   * isn't configured: the target SDK of the app.
   *
   * The `robolectric.enabledSdks` property, which selects the SDKs that tests run on, doesn't
   * apply.
   *
   * @throws IllegalStateException if no SDK that it selects is known and can run on this JVM
   */
  public fun plan(config: Config): List<Configuration>

  /**
   * Sets up the Android environment for a configuration that [plan] returned, and returns it. The
   * caller closes it when the code that shares its state has run.
   *
   * Environments that are open at the same time never share a sandbox, so they don't affect each
   * other, whether they are for the same configuration or not.
   *
   * @throws IllegalArgumentException if the configuration isn't one that this API planned
   */
  public fun open(configuration: Configuration): RobolectricEnvironment

  /** Closes the environments that are still open, and releases the sandboxes of this session. */
  override fun close()

  /** Configures and creates a session. */
  public class Builder internal constructor() {
    private var properties: Properties = System.getProperties()
    private val sharedPackages = mutableListOf<String>()
    private var listener: RobolectricSessionListener? = null

    /**
     * Sets the properties that configure Robolectric, such as `robolectric.enabledSdks`, instead of
     * the system properties.
     */
    public fun properties(properties: Properties): Builder = apply { this.properties = properties }

    /**
     * Makes the sandboxes use the classes of a package, such as `org.example.framework.`, as they
     * are, instead of loading them again. A test framework needs that for the classes it shares
     * with the tests it runs in an environment, such as its assertion errors.
     *
     * The classes whose names start with the given name are shared, so it can also be the start of
     * the names of some classes of a package.
     */
    public fun sharePackage(packageName: String): Builder = apply {
      sharedPackages.add(packageName)
    }

    /** Sets the listener that is told what the session does. */
    public fun listener(listener: RobolectricSessionListener): Builder = apply {
      this.listener = listener
    }

    /** Creates the session. */
    public fun build(): RobolectricSession =
      DefaultRobolectricSession(properties, sharedPackages.toList(), listener)
  }

  public companion object {
    /** Creates a session configured by the system properties. */
    @JvmStatic public fun create(): RobolectricSession = builder().build()

    /** Returns a builder for a session. */
    @JvmStatic public fun builder(): Builder = Builder()
  }
}
