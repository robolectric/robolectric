package org.robolectric.enginemeta

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.robolectric.enginefixtures.EngineParityFixture
import org.robolectric.enginefixtures.ExtensionParityFixture
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.junit.jupiter.RobolectricJupiterEngine

/**
 * Executes twin fixtures — identical test bodies, one per Jupiter path — and asserts both paths
 * produce identical per-test outcomes, so the custom engine and [RobolectricExtension] cannot
 * silently drift apart again.
 */
class EnginePathParityTest {

  @Test
  fun bothJupiterPathsProduceIdenticalOutcomes() {
    val engineResults = runOnCustomEngine(EngineParityFixture::class.java)
    val extensionResults = runOnStandardEngine(ExtensionParityFixture::class.java)

    assertThat(engineResults)
      .containsExactly(
        "passes",
        TestExecutionResult.Status.SUCCESSFUL,
        "fails",
        TestExecutionResult.Status.FAILED,
        "runsOnSdk34",
        TestExecutionResult.Status.SUCCESSFUL,
      )
    assertThat(extensionResults).isEqualTo(engineResults)
  }

  private fun runOnCustomEngine(fixture: Class<*>): Map<String, TestExecutionResult.Status> {
    val engine = RobolectricJupiterEngine()
    val request =
      LauncherDiscoveryRequestBuilder.request()
        .selectors(DiscoverySelectors.selectClass(fixture))
        .build()
    val root = engine.discover(request, UniqueId.forEngine(engine.id))
    val listener = RecordingEngineListener()
    engine.execute(ExecutionRequest.create(root, listener, request.configurationParameters))
    return listener.results
  }

  private fun runOnStandardEngine(fixture: Class<*>): Map<String, TestExecutionResult.Status> {
    val listener = RecordingLauncherListener()
    val request =
      LauncherDiscoveryRequestBuilder.request()
        .selectors(DiscoverySelectors.selectClass(fixture))
        .filters(EngineFilter.includeEngines("junit-jupiter"))
        .build()
    LauncherFactory.create().execute(request, listener)
    return listener.results
  }

  private class RecordingEngineListener : EngineExecutionListener {
    val results = mutableMapOf<String, TestExecutionResult.Status>()

    override fun executionStarted(testDescriptor: TestDescriptor) = Unit

    override fun executionFinished(
      testDescriptor: TestDescriptor,
      testExecutionResult: TestExecutionResult,
    ) {
      if (testDescriptor.isTest) {
        results[testDescriptor.displayName] = testExecutionResult.status
      }
    }

    override fun executionSkipped(testDescriptor: TestDescriptor, reason: String) = Unit

    override fun dynamicTestRegistered(testDescriptor: TestDescriptor) = Unit

    override fun reportingEntryPublished(testDescriptor: TestDescriptor, entry: ReportEntry) = Unit
  }

  private class RecordingLauncherListener : TestExecutionListener {
    val results = mutableMapOf<String, TestExecutionResult.Status>()

    override fun executionFinished(
      testIdentifier: TestIdentifier,
      testExecutionResult: TestExecutionResult,
    ) {
      if (testIdentifier.isTest) {
        results[testIdentifier.displayName.removeSuffix("()")] = testExecutionResult.status
      }
    }
  }
}
