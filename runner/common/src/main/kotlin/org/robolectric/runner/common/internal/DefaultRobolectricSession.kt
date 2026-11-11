package org.robolectric.runner.common.internal

import java.lang.reflect.Method
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.Locale
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment
import org.robolectric.runner.common.RobolectricSession
import org.robolectric.runner.common.RobolectricSessionListener
import org.robolectric.runner.common.SandboxKey

/**
 * Plans and opens environments the way `RobolectricTestRunner` runs a test: a [Planner] selects the
 * SDKs, then the session configures a sandbox as the runner does and sets up the application state.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class DefaultRobolectricSession(
  properties: Properties,
  sharedPackages: List<String>,
  /** The listener that the session's sandboxes tell about environments. */
  val listener: RobolectricSessionListener?,
  classpath: List<Path>,
  apk: Path?,
  shareSandboxes: Boolean,
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
  /** Tells which sandbox fits a configuration, and configures sandboxes. */
  val setup = SandboxSetup(injector, sharedPackages, entries?.findResource(KOTLIN_CLASS) != null)

  private val planner = Planner(injector, properties, entries, apk)
  private val sandboxes: Sandboxes =
    if (shareSandboxes) SharedSandboxPool(injector) else SandboxPool(injector, classpath)
  private val sandboxKeys = ConcurrentHashMap<EnvironmentConfiguration, SandboxKey>()
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

  // Guarded by the lock: the sandboxes that are lent out.
  private val leases = LinkedHashSet<DefaultRobolectricSandbox>()

  override fun plan(testClass: Class<*>, testMethod: Method?): List<Configuration> {
    check(!closed) { "The session is closed" }
    return planner.plan(testClass, testMethod)
  }

  override fun plan(config: Config, vararg modes: Enum<*>): List<Configuration> {
    check(!closed) { "The session is closed" }
    return planner.plan(config, modes.toList())
  }

  override fun open(configuration: Configuration): RobolectricEnvironment {
    val start = System.nanoTime()
    val lease = lease(configuration)
    return try {
      lease.open(configuration as EnvironmentConfiguration, ownsLease = true, start)
    } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
      lease.close()
      throw e
    }
  }

  override fun sandboxKey(configuration: Configuration): SandboxKey {
    check(!closed) { "The session is closed" }
    require(configuration is EnvironmentConfiguration) {
      "The session takes a configuration that plan returned, which $configuration is not"
    }
    return sandboxKeys.computeIfAbsent(configuration, setup::key)
  }

  override fun lease(configuration: Configuration): DefaultRobolectricSandbox {
    val key = sandboxKey(configuration)
    val identity = key.identity as SandboxIdentity
    val acquired = sandboxes.acquire(identity, configuration.get(SQLiteMode.Mode::class.java))
    val lease =
      try {
        DefaultRobolectricSandbox(this, key, acquired.sandbox, acquired.created)
      } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        sandboxes.release(acquired.sandbox)
        throw e
      }
    val closedMeanwhile =
      synchronized(applicationStateLock) {
        leases.add(lease)
        closed
      }
    if (closedMeanwhile) {
      lease.close()
      throw IllegalStateException("The session is closed")
    }
    return lease
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

  /** Called by a lease once it is closed, and its environment too: gives back the sandbox. */
  fun release(lease: DefaultRobolectricSandbox) {
    if (synchronized(applicationStateLock) { leases.remove(lease) }) {
      sandboxes.release(lease.sandbox)
    }
  }

  override fun close() {
    if (closed) {
      return
    }
    closed = true
    try {
      synchronized(applicationStateLock) { openEnvironments.toList() }.forEach { it.close() }
      synchronized(applicationStateLock) { leases.toList() }.forEach { it.close() }
    } finally {
      sandboxes.shutdown()
      entries?.close()
    }
  }

  private companion object {
    /** A class of Kotlin's standard library that all Kotlin code uses. */
    private const val KOTLIN_CLASS = "kotlin/jvm/internal/Intrinsics.class"
  }
}
