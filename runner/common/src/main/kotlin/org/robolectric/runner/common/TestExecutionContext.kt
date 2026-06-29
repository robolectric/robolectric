package org.robolectric.runner.common

import java.lang.reflect.Method
import org.robolectric.internal.AndroidSandbox
import org.robolectric.manifest.AndroidManifest
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.config.ConfigurationStrategy

/**
 * Immutable context passed to test execution callbacks.
 *
 * This class provides all the information a test framework integration needs to execute a test
 * within the Robolectric sandbox.
 *
 * ## Usage
 *
 * ```kotlin
 * fun executeTest(context: TestExecutionContext) {
 *   val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(
 *     context.sandbox,
 *     context.testClass
 *   )
 *   // ... execute test
 * }
 * ```
 *
 * @property testClass The test class being executed
 * @property testMethod The test method being executed (null for class-level operations)
 * @property sandbox The Android sandbox configured for this test
 * @property configuration The resolved Robolectric configuration
 * @property sdk The Android SDK version for this test
 * @property manifest The Android manifest for this test
 */
@ExperimentalRunnerApi
data class TestExecutionContext(
  val testClass: Class<*>,
  val testMethod: Method?,
  val sandbox: AndroidSandbox,
  val configuration: ConfigurationStrategy.Configuration,
  val sdk: Sdk,
  val manifest: AndroidManifest,
) {
  /**
   * The display name for this test context.
   *
   * For method-level contexts, this is the method name. For class-level contexts, this is the
   * simple class name.
   */
  val displayName: String
    get() = testMethod?.name ?: testClass.simpleName

  /** The fully qualified test name including SDK information. */
  val qualifiedName: String
    get() = "${testClass.simpleName}.${displayName}[sdk=${sdk.apiLevel}]"

  companion object {
    /**
     * Creates a TestExecutionContext from a SandboxContext.
     *
     * @param sandboxContext The sandbox context from SandboxLifecycleManager
     * @param testClass The test class
     * @param testMethod The test method (optional)
     * @return A new TestExecutionContext
     */
    @JvmStatic
    fun fromSandboxContext(
      sandboxContext: SandboxLifecycleManager.SandboxContext,
      testClass: Class<*>,
      testMethod: Method? = null,
    ): TestExecutionContext {
      return TestExecutionContext(
        testClass = testClass,
        testMethod = testMethod,
        sandbox = sandboxContext.sandbox,
        configuration = sandboxContext.configuration,
        sdk = sandboxContext.sdk,
        manifest = sandboxContext.appManifest,
      )
    }
  }
}
