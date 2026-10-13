package org.robolectric.runner.common.internal

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Collections
import org.robolectric.annotation.Config
import org.robolectric.pluginapi.config.ConfigurationStrategy
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.pluginapi.config.Configurer
import org.robolectric.plugins.HierarchicalConfigurationStrategy.ConfigurationImpl
import org.robolectric.util.inject.Injector

/**
 * Resolves the configuration of tests with Robolectric's [ConfigurationStrategy], which reads it
 * from the test's method, its class and superclasses, and their packages. In addition, an inner
 * class is configured like the class that encloses it, unless it is configured itself.
 */
internal class Configurations(injector: Injector) {
  private val strategy = injector.getInstance(ConfigurationStrategy::class.java)
  private val configurers: Array<Configurer<*>> =
    injector.getInstance(emptyArray<Configurer<*>>().javaClass)

  /** Returns the configuration of a test, or of its class if the method is null. */
  // The strategy caches what it reads, without synchronization.
  @Synchronized
  fun get(testClass: Class<*>, method: Method?): Configuration {
    val own = strategy.getConfig(testClass, method ?: CLASS_LEVEL_METHOD)
    if (!testClass.isMemberClass || Modifier.isStatic(testClass.modifiers)) {
      return own
    }
    val inherited = get(testClass.enclosingClass, null)
    val configuration = ConfigurationImpl()
    @Suppress("UNCHECKED_CAST")
    configurers.forEach {
      overlay(it as Configurer<Any>, inherited, testClass, method, configuration)
    }
    return configuration
  }

  /**
   * Returns the global configuration with the given one applied on top of it, and with the given
   * modes, such as `GraphicsMode.Mode.NATIVE`.
   */
  fun with(config: Config, modes: List<Enum<*>>): Configuration {
    val global = get(Anchor::class.java, null)
    val configuration = ConfigurationImpl()
    configuration.map().putAll(global.map())
    for (mode in modes) {
      val type = mode.declaringJavaClass
      require(type in global.keySet()) { "${type.name} is not a mode of Robolectric" }
      configuration.map()[type] = mode
    }
    configuration.put(
      Config::class.java,
      Config.Builder(global.get(Config::class.java)).overlay(config).build(),
    )
    return configuration
  }

  /** Puts what the inner class and the method configure, on top of what the class inherits. */
  private fun <T : Any> overlay(
    configurer: Configurer<T>,
    inherited: Configuration,
    testClass: Class<*>,
    method: Method?,
    configuration: ConfigurationImpl,
  ) {
    val superclassesFirst =
      generateSequence(testClass) { it.superclass }.takeWhile { it != Any::class.java }.toList()
    var own: T? = null
    for (type in superclassesFirst.asReversed()) {
      own = merge(configurer, own, configurer.getConfigFor(type))
    }
    if (method != null) {
      own = merge(configurer, own, configurer.getConfigFor(method))
    }
    merge(configurer, inherited.get(configurer.configClass), own)?.let {
      configuration.put(configurer.configClass, it)
    }
  }

  private fun <T : Any> merge(configurer: Configurer<T>, parent: T?, child: T?): T? =
    when {
      child == null -> parent
      parent == null -> child
      else -> configurer.merge(parent, child)
    }

  /**
   * Returns the values of a configuration for an environment on one SDK: its [Config] has that SDK
   * in place of those that it selects.
   */
  fun forSdk(configuration: Configuration, apiLevel: Int): Map<Class<*>, Any> {
    val config =
      Config.Builder(configuration.get(Config::class.java))
        .setSdk(apiLevel)
        .setMinSdk(-1)
        .setMaxSdk(-1)
        .build()
    return Collections.unmodifiableMap(configuration.map() + (Config::class.java to config))
  }

  /** Returns what the values are for an environment: equal if they make them interchangeable. */
  fun identity(values: Map<Class<*>, Any>): Any = values.mapValues { (_, value) ->
    if (value is Config) configKey(value) else value
  }

  /** A class without configuration, whose method stands for "no test method". */
  private class Anchor {
    @Suppress("unused") fun robolectricClassLevelEnvironment() = Unit
  }

  private companion object {
    private val CLASS_LEVEL_METHOD: Method =
      Anchor::class.java.getDeclaredMethod("robolectricClassLevelEnvironment")

    /** The values of a [Config], which doesn't implement equality itself. */
    private fun configKey(config: Config): Map<String, Any?> =
      Config::class
        .java
        .declaredMethods
        .filter { it.parameterCount == 0 }
        .associate { method -> method.name to comparable(method.invoke(config)) }

    /** Returns the value, or the list of its elements if it is an array. */
    private fun comparable(value: Any?): Any? =
      if (value != null && value.javaClass.isArray) {
        List(java.lang.reflect.Array.getLength(value)) { java.lang.reflect.Array.get(value, it) }
      } else {
        value
      }
  }
}
