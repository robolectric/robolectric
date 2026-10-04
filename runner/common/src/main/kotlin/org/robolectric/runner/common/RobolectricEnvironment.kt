package org.robolectric.runner.common

import java.util.concurrent.Callable
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration

/**
 * An Android environment: an application set up in a Robolectric sandbox for one Android SDK. It is
 * ready to use when it is returned, and torn down when it is closed.
 *
 * Android classes exist only inside the sandbox, so code that uses them has to be loaded by
 * [classLoader] and run with [run].
 */
@ExperimentalRunnerApi
public interface RobolectricEnvironment : AutoCloseable {
  /**
   * The configuration that this environment was opened for. Its `Config` has the SDK of the
   * environment as its only one.
   */
  public val configuration: Configuration

  /** The class loader of the sandbox, which loads the Android SDK and instrumented classes. */
  public val classLoader: ClassLoader

  /**
   * Runs the action on the main thread of the sandbox, which is Android's main thread, with
   * [classLoader] as the context class loader, and returns its result. An exception the action
   * throws is rethrown as it is. Calling this from inside an action runs the nested action in
   * place.
   *
   * @throws IllegalStateException if the environment is closed
   */
  @Throws(Exception::class) public fun <T> run(action: Callable<T>): T

  /**
   * Runs the action as a task of Android's main looper, and returns its result once the looper ran
   * it. Other than [run], it doesn't wait for the main thread to be free: it is how other threads
   * reach Android while a call of [run] keeps that thread, with a loop that runs the looper, as a
   * simulator does. Calling this from the main thread runs the action in place.
   *
   * @throws IllegalStateException if the environment is closed
   */
  @Throws(Exception::class) public fun <T> post(action: Callable<T>): T

  /**
   * Returns the class with the same name as the given one that [classLoader] loads: its twin in the
   * sandbox, which can use Android classes. Classes that the sandbox doesn't reload, such as those
   * of the JDK, are returned as they are.
   */
  public fun loadClass(original: Class<*>): Class<*>

  /**
   * Adds what the environment knows about why a test that ran in it may have failed, such as tasks
   * that the test left unexecuted on the main looper, to the failure as suppressed exceptions, as
   * `RobolectricTestRunner` does.
   */
  public fun diagnoseFailure(failure: Throwable)

  /** Tears down the environment. Closing it again does nothing. */
  override fun close()
}
