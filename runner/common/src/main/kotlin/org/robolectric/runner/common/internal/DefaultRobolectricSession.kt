package org.robolectric.runner.common.internal

import java.lang.reflect.Method
import java.net.URLClassLoader
import java.nio.file.Path
import java.time.Duration
import java.util.Locale
import java.util.Properties
import org.robolectric.android.AndroidSdkShadowMatcher
import org.robolectric.annotation.Config
import org.robolectric.config.AndroidConfigurer
import org.robolectric.interceptors.AndroidInterceptors
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.bytecode.ClassHandlerBuilder
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.internal.bytecode.Interceptors
import org.robolectric.internal.bytecode.ShadowProviders
import org.robolectric.pluginapi.MethodHandleDecorator
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment
import org.robolectric.runner.common.RobolectricSession
import org.robolectric.runner.common.RobolectricSessionListener

/**
 * Plans and opens environments the way `RobolectricTestRunner` runs a test: a [Planner] selects the
 * SDKs, then the session configures a sandbox as the runner does and sets up the application state.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class DefaultRobolectricSession(
  properties: Properties,
  private val sharedPackages: List<String>,
  private val listener: RobolectricSessionListener?,
  classpath: List<Path>,
  apk: Path?,
) : RobolectricSession {
  // The entries of a class path of its own. Robolectric's plugins are found in them too, as the
  // simulator found them in the jars of an app.
  private val entries: URLClassLoader? =
    classpath
      .takeIf { it.isNotEmpty() }
      ?.let { paths ->
        val urls = paths.map { it.toUri().toURL() }.toTypedArray()
        URLClassLoader(urls, Injectors.contextClassLoader())
      }
  private val injector = Injectors.create(properties, entries)
  private val androidConfigurer = injector.getInstance(AndroidConfigurer::class.java)
  private val shadowProviders = injector.getInstance(ShadowProviders::class.java)
  private val classHandlerBuilder = injector.getInstance(ClassHandlerBuilder::class.java)
  private val methodHandleDecorators =
    injector.getInstance(Array<MethodHandleDecorator>::class.java).toList()
  private val interceptors = Interceptors(AndroidInterceptors.all())

  private val planner = Planner(injector, properties, entries, apk)
  private val sandboxes = SandboxPool(injector, classpath)
  @Volatile private var closed = false

  /**
   * Held while the application state of an environment is set up or torn down. That touches state
   * of the whole JVM, such as security providers, so one environment does it at a time.
   */
  val applicationStateLock = Any()

  // Guarded by the lock: the open environments, oldest first, and the default locale that the JVM
  // had before the first of them was set up.
  private val openEnvironments = LinkedHashSet<DefaultRobolectricEnvironment>()
  private var localeWithoutEnvironments: Locale? = null

  override fun plan(testClass: Class<*>, testMethod: Method?): List<Configuration> {
    check(!closed) { "The session is closed" }
    return planner.plan(testClass, testMethod)
  }

  override fun plan(config: Config, vararg modes: Enum<*>): List<Configuration> {
    check(!closed) { "The session is closed" }
    return planner.plan(config, modes.toList())
  }

  override fun open(configuration: Configuration): RobolectricEnvironment {
    check(!closed) { "The session is closed" }
    require(configuration is EnvironmentConfiguration) {
      "open takes a configuration that plan returned, which $configuration is not"
    }
    val start = System.nanoTime()
    val config = configuration.get(Config::class.java)
    val lease =
      sandboxes.acquire(createInstrumentationConfig(config), configuration.sdk, configuration)
    val environment =
      try {
        // Configure the sandbox before its class loader is used, as the test runner does.
        configureSandbox(lease.sandbox, config, configuration.sdk)
        DefaultRobolectricEnvironment(this, configuration, lease.sandbox).also { it.setUp() }
      } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        sandboxes.release(lease.sandbox)
        throw e
      }
    val duration = Duration.ofNanos(System.nanoTime() - start)
    listener?.environmentOpened(configuration, lease.created, duration)
    return environment
  }

  /** Called by an environment, with the lock held, before it sets up its application state. */
  fun beforeSetUp() {
    if (openEnvironments.isEmpty()) {
      localeWithoutEnvironments = Locale.getDefault()
    }
  }

  /** Called by an environment, with the lock held, once its application state is set up. */
  fun afterSetUp(environment: DefaultRobolectricEnvironment) {
    openEnvironments.add(environment)
  }

  /**
   * Called by an environment, with the lock held, once its application state is torn down, or
   * failed to be set up. Android sets the JVM's default locale for an environment, and resets it to
   * what it was when the sandbox was created, which is wrong for environments that overlap: the
   * environments that are still open keep theirs, and the JVM gets its own back after the last.
   */
  fun afterTearDown(environment: DefaultRobolectricEnvironment) {
    openEnvironments.remove(environment)
    (openEnvironments.lastOrNull()?.locale ?: localeWithoutEnvironments)?.let {
      Locale.setDefault(it)
    }
  }

  /** Called by an environment once it is closed. */
  fun onClosed(
    environment: DefaultRobolectricEnvironment,
    sandbox: AndroidSandbox,
    duration: Duration,
  ) {
    sandboxes.release(sandbox)
    listener?.environmentClosed(environment.configuration, duration)
  }

  override fun close() {
    if (closed) {
      return
    }
    closed = true
    try {
      synchronized(applicationStateLock) { openEnvironments.toList() }.forEach { it.close() }
    } finally {
      sandboxes.shutdown()
      entries?.close()
    }
  }

  private fun createInstrumentationConfig(config: Config): InstrumentationConfiguration {
    val builder = InstrumentationConfiguration.newBuilder()
    val customPackages = System.getProperty("org.robolectric.packagesToNotAcquire", "").split(',')
    (customPackages.filter { it.isNotEmpty() } + sharedPackages).forEach {
      builder.doNotAcquirePackage(it)
    }
    val classesToNotInstrument =
      System.getProperty("org.robolectric.classesToNotInstrumentRegex", "")
    if (classesToNotInstrument.isNotEmpty()) {
      builder.setDoNotInstrumentClassRegex(classesToNotInstrument)
    }
    androidConfigurer.configure(builder, interceptors)
    androidConfigurer.withConfig(builder, config)
    if (entries?.findResource(KOTLIN_CLASS) != null) {
      // An app that brings Kotlin's standard library, such as one that its build has desugared,
      // gets that in its sandbox, where a test shares that of the JVM.
      builder.packagesToNotAcquire.remove(KOTLIN_PACKAGE)
    }
    return builder.build()
  }

  private fun configureSandbox(sandbox: AndroidSandbox, config: Config, sdk: Sdk) {
    val shadowMapBuilder = shadowProviders.baseShadowMap.newBuilder()
    config.shadows.forEach { shadowMapBuilder.addShadowClasses(it.java) }
    val shadowMap = shadowMapBuilder.build()
    sandbox.replaceShadowMap(shadowMap)
    sandbox.configure(
      classHandlerBuilder.build(
        shadowMap,
        AndroidSdkShadowMatcher(sdk.apiLevel),
        interceptors,
        methodHandleDecorators,
      ),
      interceptors,
    )
  }

  private companion object {
    private const val KOTLIN_PACKAGE = "kotlin."

    /** A class of Kotlin's standard library that all Kotlin code uses. */
    private const val KOTLIN_CLASS = "kotlin/jvm/internal/Intrinsics.class"
  }
}
