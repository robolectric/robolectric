@file:OptIn(org.robolectric.runner.common.ExperimentalRunnerApi::class)

package org.robolectric.junit.platform

import java.util.concurrent.ConcurrentHashMap
import org.junit.platform.engine.UniqueId
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.RobolectricSandbox

internal class ActiveSandbox(
  val sandbox: RobolectricSandbox,
  private val configurations: Map<UniqueId, Configuration>,
  val classConfiguration: Configuration,
) {
  fun configuration(id: String): Configuration =
    configurations.entries
      .filter { UniqueId.parse(id).hasPrefix(it.key) }
      .maxByOrNull { it.key.segments.size }
      ?.value ?: classConfiguration
}

internal object ActiveSandboxes {
  private val active = ConcurrentHashMap<ClassLoader, ActiveSandbox>()

  fun register(context: ActiveSandbox) {
    check(active.putIfAbsent(context.sandbox.classLoader, context) == null) {
      "Concurrent Jupiter launchers cannot execute in the same Robolectric sandbox"
    }
  }

  fun remove(context: ActiveSandbox) {
    active.remove(context.sandbox.classLoader, context)
  }

  fun forClass(type: Class<*>): ActiveSandbox =
    checkNotNull(active[type.classLoader]) {
      "${type.name} requires the 'robolectric' JUnit Platform engine. Add " +
        "org.robolectric:runner-junit-jupiter and enable useJUnitPlatform() without excluding " +
        "the robolectric engine."
    }
}
