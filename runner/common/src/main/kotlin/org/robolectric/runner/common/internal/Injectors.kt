package org.robolectric.runner.common.internal

import java.util.Properties
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.config.GlobalConfigProvider
import org.robolectric.util.inject.Injector

/** Creates the injector of a session, which finds and creates Robolectric's plugins. */
internal object Injectors {
  private const val SERVICES = "META-INF/services/"
  private const val TEST_RUNNER_DEFAULT =
    "org.robolectric.RobolectricTestRunner\$DeprecatedTestRunnerDefaultConfigProvider"

  /** Creates an injector that finds plugins with the class loader, if there is one. */
  fun create(properties: Properties, plugins: ClassLoader? = null): Injector {
    val builder = if (plugins == null) Injector.Builder() else Injector.Builder(plugins)
    builder.bind(Properties::class.java, properties)
    // Robolectric's default GlobalConfigProvider is a nested class of RobolectricTestRunner, which
    // can't be created without JUnit 4. A session doesn't depend on JUnit 4, so it provides the
    // same default itself, unless a plugin provides another one.
    if (globalConfigProviders(plugins).all { it == TEST_RUNNER_DEFAULT }) {
      builder.bind(
        GlobalConfigProvider::class.java,
        GlobalConfigProvider { Config.Builder().build() },
      )
    }
    return builder.build()
  }

  /** Returns the names of the plugins that provide the global configuration. */
  private fun globalConfigProviders(plugins: ClassLoader?): List<String> {
    val classLoader = plugins ?: contextClassLoader()
    return classLoader
      .getResources(SERVICES + GlobalConfigProvider::class.java.name)
      .asSequence()
      .flatMap { url -> url.openStream().bufferedReader().use { it.readLines() } }
      .map { it.substringBefore('#').trim() }
      .filter { it.isNotEmpty() }
      .toList()
  }

  /** Returns the class loader that plugins are found with by default. */
  fun contextClassLoader(): ClassLoader =
    Thread.currentThread().contextClassLoader ?: javaClass.classLoader
}
