package org.robolectric.junit.platform

import java.util.ServiceLoader
import org.junit.platform.engine.ConfigurationParameters
import org.junit.platform.engine.DiscoveryFilter
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.UniqueId
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.core.LauncherConfig
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory

internal class JupiterDelegate(private val parameters: ConfigurationParameters) {
  private fun launcher() =
    LauncherFactory.create(
      LauncherConfig.builder()
        .enableTestEngineAutoRegistration(false)
        .enableLauncherSessionListenerAutoRegistration(false)
        .enableLauncherDiscoveryListenerAutoRegistration(false)
        .enablePostDiscoveryFilterAutoRegistration(false)
        .enableTestExecutionListenerAutoRegistration(false)
        .addTestEngines(
          ServiceLoader.load(TestEngine::class.java, javaClass.classLoader).firstOrNull {
            it.id == "junit-jupiter"
          } ?: error("The JUnit Jupiter engine is required to run Robolectric Jupiter tests"),
        )
        .build(),
    )

  fun request(
    selectors: List<DiscoverySelector>,
    filters: List<DiscoveryFilter<*>> = emptyList(),
  ): LauncherDiscoveryRequest =
    LauncherDiscoveryRequestBuilder.request()
      .selectors(selectors)
      .filters(*filters.toTypedArray())
      .parentConfigurationParameters(parameters)
      // Tests run sequentially on Android's main thread.
      .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
      .build()

  fun discover(
    selectors: List<DiscoverySelector>,
    filters: List<DiscoveryFilter<*>> = emptyList(),
  ) = launcher().discover(request(selectors, filters))

  fun execute(selectors: List<DiscoverySelector>, listener: TestExecutionListener) {
    launcher().execute(request(selectors), listener)
  }

  companion object {
    fun outer(id: UniqueId, parent: UniqueId): UniqueId =
      id.segments.drop(1).fold(parent) { result, segment ->
        result.append(segment.type, segment.value)
      }

    fun inner(id: UniqueId, parent: UniqueId): UniqueId =
      id.segments.drop(parent.segments.size).fold(UniqueId.forEngine("junit-jupiter")) {
        result,
        segment ->
        result.append(segment.type, segment.value)
      }
  }
}
