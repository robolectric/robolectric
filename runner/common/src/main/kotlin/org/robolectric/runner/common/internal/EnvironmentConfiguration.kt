package org.robolectric.runner.common.internal

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
  /** What the values are for an environment: equal if they make them interchangeable. */
  private val identity: Any,
) : Configuration {
  @Suppress("UNCHECKED_CAST")
  override fun <T> get(configClass: Class<T>): T = values[configClass] as T

  override fun keySet(): Collection<Class<*>> = values.keys

  override fun map(): Map<Class<*>, Any> = values

  override fun equals(other: Any?): Boolean =
    other is EnvironmentConfiguration && identity == other.identity

  override fun hashCode(): Int = identity.hashCode()

  override fun toString(): String = "Configuration(sdk=${sdk.apiLevel}, name=$name)"
}
