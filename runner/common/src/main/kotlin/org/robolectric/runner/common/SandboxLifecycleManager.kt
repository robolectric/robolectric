package org.robolectric.runner.common

import java.lang.reflect.Method
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.ResourcesMode
import org.robolectric.annotation.SQLiteMode
import org.robolectric.internal.AndroidSandbox
import org.robolectric.manifest.AndroidManifest
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.config.ConfigurationStrategy

/**
 * Manages the complete lifecycle of Robolectric sandboxes.
 *
 * This class provides a high-level API for:
 * - Creating and configuring sandboxes
 * - Setting up the Android environment
 * - Tearing down the environment
 *
 * ## Usage
 *
 * ```kotlin
 * val deps = RobolectricDependencies.create()
 * val manager = SandboxLifecycleManager(deps)
 *
 * // Create sandbox for a test
 * val sandbox = manager.createSandbox(testClass, testMethod)
 *
 * // Execute test in sandbox
 * manager.executeInSandbox(sandbox, testClass, testMethod, "testName") {
 *   // test code runs here
 * }
 *
 * // Sandbox is automatically cleaned up
 * ```
 */
@ExperimentalRunnerApi
class SandboxLifecycleManager(private val dependencies: RobolectricDependencies) {
  private val configurator =
    SandboxConfigurator(
      dependencies.androidConfigurer,
      dependencies.shadowProviders,
      dependencies.classHandlerBuilder,
    )

  /**
   * Creates and configures a sandbox for the given test class and method.
   *
   * This method returns only the first SDK context. For multi-SDK execution, use [createSandboxes].
   *
   * @param testClass The test class
   * @param testMethod The test method (optional, for method-level configuration)
   * @return A configured SandboxContext containing the sandbox and related configuration
   */
  fun createSandbox(testClass: Class<*>, testMethod: Method? = null): SandboxContext {
    return createSandboxes(testClass, testMethod).first()
  }

  /**
   * Creates and configures sandboxes for all selected SDKs for the given test class and method.
   *
   * This method is used for multi-SDK test execution when `-Drobolectric.enabledSdks` is specified.
   * Each returned context represents a separate test execution with a different SDK.
   *
   * @param testClass The test class
   * @param testMethod The test method (optional, for method-level configuration)
   * @return A list of configured SandboxContext instances, one per selected SDK
   */
  @Suppress("LongMethod")
  fun createSandboxes(testClass: Class<*>, testMethod: Method? = null): List<SandboxContext> {
    val method = testMethod ?: testClass.methods.firstOrNull()
    checkNotNull(method) { "No methods found in test class ${testClass.name}" }

    val configuration = dependencies.configurationStrategy.getConfig(testClass, method)
    val config = configuration.get(Config::class.java)

    val appManifest = ManifestResolver.resolveManifest(config)

    // Get SDKs after enabledSdks filtering
    var sdks = dependencies.sdkPicker.selectSdks(configuration, appManifest)

    // Log SDK selection
    RunnerLogger.logSdkSelection(
      testClass.simpleName,
      sdks.map { it.apiLevel },
      config.sdk.toList(),
    )

    // FALLBACK LOGIC: If filtering removed all SDKs, use original config
    //
    // IMPLEMENTATION NOTE: We use reflection to access SdkCollection because:
    // 1. SdkCollection is defined in :robolectric module
    // 2. :runner:common only has an implementation dependency on :robolectric (not api)
    // 3. Changing to api dependency would expose internal classes in the public API
    // 4. Reflection keeps the API clean while providing necessary functionality
    //
    // If SdkCollection is moved to :pluginapi in the future, this can be replaced
    // with direct typed access.
    if (sdks.isEmpty()) {
      // Get SdkCollection from injector (it's in the :robolectric module)
      val sdkCollection =
        try {
          dependencies.injector.getInstance(Class.forName("org.robolectric.plugins.SdkCollection"))
        } catch (@Suppress("SwallowedException") e: ClassNotFoundException) {
          null
        }
      if (sdkCollection != null) {
        val configuredSdks = config.sdk
        if (configuredSdks.isNotEmpty()) {
          // Use first SDK from @Config annotation
          val getSdkMethod =
            sdkCollection.javaClass.getMethod("getSdk", Int::class.javaPrimitiveType)
          val fallbackSdk = getSdkMethod.invoke(sdkCollection, configuredSdks[0]) as Sdk
          val enabledSdksProperty = System.getProperty("robolectric.enabledSdks", "")
          val reason = "enabledSdks=$enabledSdksProperty filtered out configured SDKs"
          RunnerLogger.logSdkFallback(
            testClass.simpleName,
            method.name,
            fallbackSdk.apiLevel,
            reason,
          )
          System.err.println(
            "WARNING: System property 'robolectric.enabledSdks=$enabledSdksProperty' " +
              "filtered out all configured SDKs for ${testClass.simpleName}.${method.name}. " +
              "Configured SDKs: ${configuredSdks.joinToString(",")}. " +
              "Falling back to SDK ${fallbackSdk.apiLevel}. " +
              "To avoid this warning, update @Config(sdk=[...]) to include one of the enabled SDKs."
          )
          sdks = listOf(fallbackSdk)
        } else {
          // No @Config SDKs, use default (latest known SDK)
          val getKnownSdksMethod = sdkCollection.javaClass.getMethod("getKnownSdks")
          @Suppress("UNCHECKED_CAST")
          val knownSdks = getKnownSdksMethod.invoke(sdkCollection) as java.util.SortedSet<Sdk>
          val fallbackSdk = knownSdks.last()
          RunnerLogger.logSdkFallback(
            testClass.simpleName,
            method.name,
            fallbackSdk.apiLevel,
            "no @Config SDKs",
          )
          System.err.println(
            "WARNING: Using default SDK ${fallbackSdk.apiLevel} for ${testClass.simpleName}.${method.name}"
          )
          sdks = listOf(fallbackSdk)
        }
      }
    }

    check(sdks.isNotEmpty()) {
      "No SDKs found for test class ${testClass.name}. " +
        "Ensure Android SDK jars are available. " +
        "Configure sdk in @Config annotation or check robolectric-deps.properties."
    }

    val instConfig = configurator.createInstrumentationConfig(config)

    return sdks.map { sdk ->
      // Record metrics and time sandbox creation
      RunnerMetrics.recordSandboxCreation()
      val sandbox =
        RunnerMetrics.timed(RunnerMetrics.PHASE_SANDBOX_CREATION) {
          dependencies.sandboxManager.getAndroidSandbox(
            instConfig,
            sdk,
            configuration.get(ResourcesMode.Mode::class.java),
            configuration.get(LooperMode.Mode::class.java),
            configuration.get(SQLiteMode.Mode::class.java),
            configuration.get(GraphicsMode.Mode::class.java),
          )
        }

      configurator.configureSandbox(sandbox, config, sdk)

      // Log sandbox creation details
      RunnerLogger.logSandboxCreation(
        RunnerLogger.SandboxCreationInfo(
          testClass = testClass.simpleName,
          testMethod = testMethod?.name,
          sdkApiLevel = sdk.apiLevel,
          looperMode = configuration.get(LooperMode.Mode::class.java).name,
          sqliteMode = configuration.get(SQLiteMode.Mode::class.java).name,
          graphicsMode = configuration.get(GraphicsMode.Mode::class.java).name,
        )
      )

      SandboxContext(
        sandbox = sandbox,
        configuration = configuration,
        config = config,
        appManifest = appManifest,
        sdk = sdk,
      )
    }
  }

  /**
   * Executes a block of code within the sandbox environment.
   *
   * This method handles:
   * - Thread context classloader setup
   * - Android environment initialization
   * - Proper cleanup after execution
   *
   * @param context The sandbox context
   * @param testName A descriptive name for the test
   * @param block The code to execute
   * @return The result of the block
   */
  fun <T> executeInSandbox(
    context: SandboxContext,
    testName: String,
    configuration: ConfigurationStrategy.Configuration? = null,
    block: () -> T,
  ): T {
    val environment = getEnvironment(context, configuration)
    return environment.executeInSandbox(testName, block)
  }

  /** Creates a RobolectricEnvironment for the given context. */
  fun getEnvironment(
    context: SandboxContext,
    configuration: ConfigurationStrategy.Configuration? = null,
  ): RobolectricEnvironment {
    return RobolectricEnvironment(
      context.sandbox,
      configuration ?: context.configuration,
      context.appManifest,
    )
  }

  /** Contains all the context needed to run tests in a sandbox. */
  data class SandboxContext(
    val sandbox: AndroidSandbox,
    val configuration: ConfigurationStrategy.Configuration,
    val config: Config,
    val appManifest: AndroidManifest,
    val sdk: Sdk,
  )
}
