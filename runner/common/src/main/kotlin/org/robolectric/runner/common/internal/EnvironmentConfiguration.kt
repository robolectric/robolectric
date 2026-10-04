package org.robolectric.runner.common.internal

import org.robolectric.annotation.Config
import org.robolectric.manifest.AndroidManifest
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration

/**
 * The configuration of one environment: that of a test or of a tool, in which the `Config` has the
 * SDK of the environment as its only one. Two are equal if their environments are interchangeable.
 */
internal class EnvironmentConfiguration(
  val sdk: Sdk,
  private val values: Map<Class<*>, Any>,
  val manifest: AndroidManifest,
  /** Names the environment's temporary directory, as the test it was planned for. */
  val name: String,
  /**
   * The packages that `@SandboxConfig` of the test instruments, in addition to the default ones.
   */
  val instrumentedPackages: List<String>,
) : Configuration {
  private val identity: Any =
    listOf(
      values.mapValues { (_, value) -> if (value is Config) configKey(value) else value },
      instrumentedPackages,
    )

  @Suppress("UNCHECKED_CAST")
  override fun <T> get(configClass: Class<T>): T = values[configClass] as T

  override fun keySet(): Collection<Class<*>> = values.keys

  override fun map(): Map<Class<*>, Any> = values

  override fun equals(other: Any?): Boolean =
    other is EnvironmentConfiguration && identity == other.identity

  override fun hashCode(): Int = identity.hashCode()

  override fun toString(): String = "Configuration(sdk=${sdk.apiLevel}, name=$name)"

  private companion object {
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
