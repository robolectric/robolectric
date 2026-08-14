package org.robolectric.enginemeta

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.robolectric.enginefixtures.MultiSdkFixture
import org.robolectric.enginefixtures.MultiSdkWithLifecycleFixture
import org.robolectric.junit.jupiter.RobolectricJupiterEngine

/**
 * Multi-SDK discovery/execution parity for [RobolectricJupiterEngine]: `@Config(sdk = [a, b])`
 * expands into one descriptor per SDK (like the Platform engine), each variant executes on its SDK,
 * and variants conflicting with a shared class environment fail fast.
 *
 * `robolectric.enabledSdks` is cleared for the nested engine runs so the CI SDK matrix cannot
 * reshape the fixtures' pinned SDK sets.
 */
class MultiSdkEngineTest {

  private var savedEnabledSdks: String? = null

  @BeforeEach
  fun clearEnabledSdks() {
    savedEnabledSdks = System.getProperty(ENABLED_SDKS_PROPERTY)
    System.clearProperty(ENABLED_SDKS_PROPERTY)
  }

  @AfterEach
  fun restoreProperties() {
    savedEnabledSdks?.let { System.setProperty(ENABLED_SDKS_PROPERTY, it) }
      ?: System.clearProperty(ENABLED_SDKS_PROPERTY)
    System.clearProperty(MultiSdkFixture.SEEN_PROPERTY)
  }

  @Test
  fun multiSdkMethodExpandsIntoPerSdkDescriptors() {
    val root = discover(MultiSdkFixture::class.java)
    val tests = root.descendants.filter { it.isTest }

    assertThat(tests).hasSize(2)
    // Default naming: every variant except the last carries the SDK marker.
    assertThat(tests.map { it.displayName }).containsExactly("runsOnBoth[33]", "runsOnBoth")
    assertThat(tests.map { it.uniqueId.toString() }.toSet()).hasSize(2)
  }

  @Test
  fun eachVariantExecutesOnItsOwnSdk() {
    val listener = execute(MultiSdkFixture::class.java)

    val testResults = listener.finished.filterKeys { it.isTest }.values
    assertThat(testResults).hasSize(2)
    testResults.forEach { assertThat(it.status).isEqualTo(TestExecutionResult.Status.SUCCESSFUL) }

    val seenSdks =
      System.getProperty(MultiSdkFixture.SEEN_PROPERTY, "")
        .split(",")
        .filter { it.isNotEmpty() }
        .map { it.toInt() }
    assertThat(seenSdks).containsExactly(33, 34)
  }

  @Test
  fun variantsConflictingWithClassEnvironmentFailFast() {
    val listener = execute(MultiSdkWithLifecycleFixture::class.java)

    val testResults = listener.finished.filterKeys { it.isTest }
    assertThat(testResults).hasSize(2)
    testResults.values.forEach { result ->
      assertThat(result.status).isEqualTo(TestExecutionResult.Status.FAILED)
      assertThat(result.throwable.get().message).contains("Configuration conflict")
    }
  }

  private fun discover(vararg classes: Class<*>): TestDescriptor {
    val engine = RobolectricJupiterEngine()
    val request =
      LauncherDiscoveryRequestBuilder.request()
        .selectors(classes.map { DiscoverySelectors.selectClass(it) })
        .build()
    return engine.discover(request, UniqueId.forEngine(engine.id))
  }

  private fun execute(vararg classes: Class<*>): RecordingListener {
    val engine = RobolectricJupiterEngine()
    val request =
      LauncherDiscoveryRequestBuilder.request()
        .selectors(classes.map { DiscoverySelectors.selectClass(it) })
        .build()
    val root = engine.discover(request, UniqueId.forEngine(engine.id))
    val listener = RecordingListener()
    engine.execute(ExecutionRequest.create(root, listener, request.configurationParameters))
    return listener
  }

  private class RecordingListener : EngineExecutionListener {
    val finished = mutableMapOf<TestDescriptor, TestExecutionResult>()

    override fun executionStarted(testDescriptor: TestDescriptor) = Unit

    override fun executionFinished(
      testDescriptor: TestDescriptor,
      testExecutionResult: TestExecutionResult,
    ) {
      finished[testDescriptor] = testExecutionResult
    }

    override fun executionSkipped(testDescriptor: TestDescriptor, reason: String) = Unit

    override fun dynamicTestRegistered(testDescriptor: TestDescriptor) = Unit

    override fun reportingEntryPublished(testDescriptor: TestDescriptor, entry: ReportEntry) = Unit
  }

  private companion object {
    const val ENABLED_SDKS_PROPERTY = "robolectric.enabledSdks"
  }
}
