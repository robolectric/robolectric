package org.robolectric.runner.common

import java.util.concurrent.Callable
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration

/**
 * A sandbox that [RobolectricSession.lease] lent out: Robolectric's class loader and main thread
 * for one Android SDK, without an application set up in it. The configurations of its [key] can be
 * [prepare]d in it, for code that runs before a test, and [open]ed in it, one at a time, for tests.
 *
 * It is how an integration runs the code of a test class in the same sandbox as the tests of the
 * class, as `RobolectricTestRunner` runs `@BeforeClass` methods and class rules: it prepares the
 * configuration of the class, runs the code of the class with [run], and opens an environment for
 * each test.
 */
@ExperimentalRunnerApi
public interface RobolectricSandbox : AutoCloseable {
  /** The key of the configurations that the sandbox can prepare and open. */
  public val key: SandboxKey

  /** The class loader of the sandbox, which loads the Android SDK and instrumented classes. */
  public val classLoader: ClassLoader

  /**
   * Runs the action on the main thread of the sandbox, which is Android's main thread, with
   * [classLoader] as the context class loader, and returns its result. An exception the action
   * throws is rethrown as it is. Calling this from inside an action, or from inside
   * [RobolectricEnvironment.run] of an environment of the sandbox, runs the nested action in place.
   *
   * @throws IllegalStateException if the sandbox is closed
   */
  @Throws(Exception::class) public fun <T> run(action: Callable<T>): T

  /**
   * Returns the class with the same name as the given one that [classLoader] loads, as
   * [RobolectricEnvironment.loadClass] does.
   */
  public fun loadClass(original: Class<*>): Class<*>

  /**
   * Configures the sandbox for a configuration without setting up an application: its shadows, its
   * modes, and the configuration that `ConfigurationRegistry` returns. That is the state in which
   * `RobolectricTestRunner` runs `@BeforeClass` methods and class rules, for the configuration of
   * the first test of the class.
   *
   * @throws IllegalArgumentException if the configuration isn't one that the session planned, or
   *   its key isn't that of the sandbox
   * @throws IllegalStateException if the sandbox is closed, or an environment is open in it
   */
  public fun prepare(configuration: Configuration)

  /**
   * Sets up the Android environment for a configuration in this sandbox, as
   * [RobolectricSession.open] does in a sandbox of its own, and returns it. Closing the environment
   * tears it down, and leaves the sandbox lent out, so that the next environment can be opened.
   *
   * @throws IllegalArgumentException if the configuration isn't one that the session planned, or
   *   its key isn't that of the sandbox
   * @throws IllegalStateException if the sandbox is closed, or an environment is open in it
   */
  public fun open(configuration: Configuration): RobolectricEnvironment

  /**
   * Gives the sandbox back to the session, after closing the environment that is still open in it.
   * Closing it again does nothing.
   */
  override fun close()
}
