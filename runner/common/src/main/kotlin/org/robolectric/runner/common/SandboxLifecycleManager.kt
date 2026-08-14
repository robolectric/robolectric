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
import org.robolectric.plugins.HierarchicalConfigurationStrategy

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
      dependencies.methodHandleDecorators,
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
  fun createSandbox(
    testClass: Class<*>,
    testMethod: Method? = null,
    sourceClassLoader: ClassLoader? = testClass.classLoader,
  ): SandboxContext {
    return createSandboxes(testClass, testMethod, sourceClassLoader).first()
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
  fun createSandboxes(
    testClass: Class<*>,
    testMethod: Method? = null,
    sourceClassLoader: ClassLoader? = testClass.classLoader,
  ): List<SandboxContext> {
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

    // If SdkPicker filtered everything out (e.g. -Drobolectric.enabledSdks mismatched
    // @Config(sdk=...)), fall back via SdkFallbackResolver. Returns null only if the
    // SdkCollection class itself can't be loaded — which is a hard failure.
    if (sdks.isEmpty()) {
      val fallback = SdkFallbackResolver.resolveFallbackSdk(dependencies, testClass, method, config)
      if (fallback != null) {
        sdks = listOf(fallback)
      } else {
        throw IllegalStateException(
          "No SDKs found for test class ${testClass.name}. " +
            "Ensure Android SDK jars are available. " +
            "Configure sdk in @Config annotation or check robolectric-deps.properties. " +
            "SdkCollection reflection fallback failed — see logs for details."
        )
      }
    }

    return sdks.map { sdk ->
      instantiateSandboxContext(
        configuration,
        config,
        appManifest,
        sdk,
        sourceClassLoader,
        logTestClass = testClass.simpleName,
        logTestMethod = testMethod?.name,
      )
    }
  }

  /**
   * Creates and configures a sandbox from an explicit [Config] instead of deriving configuration
   * from test-class annotations. This is the entry point for framework-free consumers (see
   * [RobolectricRuntime]) that have no test class at all.
   *
   * The configuration is resolved by overlaying [configOverlay] onto the defaults the configuration
   * strategy produces (system properties, `robolectric.properties`, global defaults).
   */
  fun createSandboxForConfig(
    configOverlay: Config,
    sourceClassLoader: ClassLoader? = null,
  ): SandboxContext {
    val anchorClass = RuntimeConfigAnchor::class.java
    val anchorMethod = anchorClass.declaredMethods.first()
    val baseConfiguration = dependencies.configurationStrategy.getConfig(anchorClass, anchorMethod)
    val mergedConfig =
      Config.Builder(baseConfiguration.get(Config::class.java)).overlay(configOverlay).build()

    val configuration = HierarchicalConfigurationStrategy.ConfigurationImpl()
    configuration.map().putAll(baseConfiguration.map())
    configuration.put(Config::class.java, mergedConfig)

    val appManifest = ManifestResolver.resolveManifest(mergedConfig)
    val sdk =
      dependencies.sdkPicker.selectSdks(configuration, appManifest).firstOrNull()
        ?: throw IllegalStateException(
          "No SDK selected for RobolectricRuntime (requested " +
            "sdk=${mergedConfig.sdk.joinToString(",")}). Ensure Android SDK jars are available " +
            "and the requested SDK is not filtered out by -Drobolectric.enabledSdks."
        )

    return instantiateSandboxContext(
      configuration,
      mergedConfig,
      appManifest,
      sdk,
      sourceClassLoader,
      logTestClass = "RobolectricRuntime",
      logTestMethod = null,
    )
  }

  @Suppress("LongParameterList")
  private fun instantiateSandboxContext(
    configuration: ConfigurationStrategy.Configuration,
    config: Config,
    appManifest: AndroidManifest,
    sdk: Sdk,
    sourceClassLoader: ClassLoader?,
    logTestClass: String,
    logTestMethod: String?,
  ): SandboxContext {
    val instConfig = configurator.createInstrumentationConfig(config)

    // Record metrics and time sandbox creation
    RunnerMetrics.recordSandboxCreation()
    val sandbox =
      RunnerMetrics.timed(RunnerMetrics.PHASE_SANDBOX_CREATION) {
        withContextClassLoader(sourceClassLoader) {
          dependencies.sandboxManager.getAndroidSandbox(
            instConfig,
            sdk,
            configuration.get(ResourcesMode.Mode::class.java),
            configuration.get(LooperMode.Mode::class.java),
            configuration.get(SQLiteMode.Mode::class.java),
            configuration.get(GraphicsMode.Mode::class.java),
          )
        }
      }

    configurator.configureSandbox(sandbox, config, sdk)

    // Log sandbox creation details
    RunnerLogger.logSandboxCreation(
      RunnerLogger.SandboxCreationInfo(
        testClass = logTestClass,
        testMethod = logTestMethod,
        sdkApiLevel = sdk.apiLevel,
        looperMode = configuration.get(LooperMode.Mode::class.java).name,
        sqliteMode = configuration.get(SQLiteMode.Mode::class.java).name,
        graphicsMode = configuration.get(GraphicsMode.Mode::class.java).name,
      )
    )

    return SandboxContext(
      sandbox = sandbox,
      configuration = configuration,
      config = config,
      appManifest = appManifest,
      sdk = sdk,
      sourceClassLoader = sourceClassLoader,
    )
  }

  /** Annotation-free anchor used to derive default configuration when no test class exists. */
  private class RuntimeConfigAnchor {
    @Suppress("unused") fun anchor() = Unit
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
      context.sourceClassLoader,
      dependencies.frameworkClassLoadingBridge,
    )
  }

  /** Contains all the context needed to run tests in a sandbox. */
  data class SandboxContext(
    val sandbox: AndroidSandbox,
    val configuration: ConfigurationStrategy.Configuration,
    val config: Config,
    val appManifest: AndroidManifest,
    val sdk: Sdk,
    val sourceClassLoader: ClassLoader?,
  )

  private fun <T> withContextClassLoader(sourceClassLoader: ClassLoader?, block: () -> T): T {
    if (sourceClassLoader == null) {
      return block()
    }
    val thread = Thread.currentThread()
    val prior = thread.contextClassLoader
    thread.contextClassLoader = sourceClassLoader
    return try {
      block()
    } finally {
      thread.contextClassLoader = prior
    }
  }
}
