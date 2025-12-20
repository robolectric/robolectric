package org.robolectric.runner.common

import org.robolectric.internal.AndroidSandbox
import org.robolectric.manifest.AndroidManifest
import org.robolectric.pluginapi.config.ConfigurationStrategy

/**
 * Manages the Robolectric test environment lifecycle within a sandbox.
 *
 * This class handles the complete lifecycle of a Robolectric test environment:
 * - Thread classloader management (save/restore)
 * - Android environment setup (application state, system properties)
 * - Test execution within sandbox context
 * - Proper cleanup and teardown
 *
 * The environment ensures that tests run with the correct classloader and that all Android
 * components are properly initialized and cleaned up.
 *
 * ## Thread Safety
 *
 * This class is NOT thread-safe. Each test should use its own instance.
 *
 * ## Usage
 *
 * ```kotlin
 * val environment = RobolectricEnvironment(sandbox, configuration, manifest)
 *
 * environment.executeInSandbox("my-test") {
 *   // Test code runs here with Android APIs available
 *   Robolectric.buildActivity(Activity::class.java).use { controller ->
 *     val activity = controller.setup().get()
 *     // ...
 *   }
 * }
 * ```
 *
 * @property sandbox The AndroidSandbox to execute tests in
 * @property configuration The test configuration
 * @property appManifest The Android manifest for the test
 */
@ExperimentalRunnerApi
class RobolectricEnvironment(
  private val sandbox: AndroidSandbox,
  private val configuration: ConfigurationStrategy.Configuration,
  private val appManifest: AndroidManifest,
) {
  private var environmentSetUp = false

  /**
   * Executes a block of code within the Robolectric sandbox environment.
   *
   * This method:
   * 1. Saves the current thread's context classloader
   * 2. Sets the sandbox's classloader as the context classloader
   * 3. Sets up the Android application state
   * 4. Executes the provided block
   * 5. Tears down the application state
   * 6. Restores the original classloader
   *
   * The block is executed with the sandbox classloader active, which ensures that all Android
   * classes are loaded through the instrumented classloader.
   *
   * @param T The return type of the block
   * @param testName A descriptive name for the test (used in environment setup)
   * @param block The code to execute within the sandbox
   * @return The result of executing the block
   * @throws Exception if the block throws an exception
   */
  fun <T> executeInSandbox(testName: String, block: () -> T): T {
    return access {
      if (!environmentSetUp) {
        try {
          setupApplicationState(testName)
          val result = block()
          return@access result
        } finally {
          if (environmentSetUp) {
            tearDownApplicationState()
          }
        }
      } else {
        // Environment already set up (persistent mode), just run the block
        block()
      }
    }
  }

  /**
   * Executes a block of code with the sandbox classloader active.
   *
   * This does NOT set up or tear down the application state. It is useful for accessing the
   * environment when it is already set up (e.g. in persistent mode).
   */
  fun <T> access(block: () -> T): T {
    val prior = Thread.currentThread().contextClassLoader
    try {
      Thread.currentThread().contextClassLoader = sandbox.robolectricClassLoader
      return block()
    } finally {
      Thread.currentThread().contextClassLoader = prior
    }
  }

  /**
   * Sets up the Android application state within the sandbox.
   *
   * This method initializes:
   * - Android system properties
   * - Application instance
   * - Looper configuration
   * - Resource loading
   *
   * This is called automatically by [executeInSandbox].
   *
   * @param testName A descriptive name for the test
   */
  fun setupApplicationState(testName: String) {
    val env = sandbox.testEnvironment
    env.setUpApplicationState(testName, configuration, appManifest)
    environmentSetUp = true
  }

  /**
   * Tears down the Android application state and resets the environment.
   *
   * This method cleans up:
   * - Application instance
   * - System properties
   * - Looper state
   * - Static field state
   *
   * This is called automatically by [executeInSandbox].
   */
  fun tearDownApplicationState() {
    val env = sandbox.testEnvironment
    env.tearDownApplication()
    env.resetState()
    environmentSetUp = false
  }

  /**
   * Gets the sandbox's Robolectric classloader.
   *
   * This classloader is used to load Android classes with instrumentation applied.
   *
   * @return The sandbox's classloader
   */
  fun getSandboxClassLoader(): ClassLoader = sandbox.robolectricClassLoader
}
