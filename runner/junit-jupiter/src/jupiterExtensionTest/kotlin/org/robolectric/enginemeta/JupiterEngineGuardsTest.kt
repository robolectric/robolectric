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
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.robolectric.enginefixtures.DisabledClassFixture
import org.robolectric.enginefixtures.DisabledMethodFixture
import org.robolectric.enginefixtures.ExtensionOptedInFixture
import org.robolectric.enginefixtures.UnsupportedKindsFixture
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.junit.jupiter.RobolectricJupiterEngine

/**
 * Guard behavior of [RobolectricJupiterEngine]: no silent drops, no double runs.
 * - Classes opted into [RobolectricExtension] are skipped at discovery (the standard engine owns
 *   them).
 * - `@Disabled` classes/methods are reported skipped, never executed.
 * - `@TestTemplate`/`@TestFactory`-kind methods are discovered and fail loudly instead of
 *   vanishing.
 */
class JupiterEngineGuardsTest {

  @Test
  fun extendWithClassesAreSkippedDuringDiscovery() {
    val root = discover(ExtensionOptedInFixture::class.java)

    assertThat(root.children).isEmpty()
  }

  @Test
  fun disabledMethodIsReportedSkippedNotExecuted() {
    val listener = execute(DisabledMethodFixture::class.java)

    val skipped = listener.skipped.keys.single()
    assertThat(skipped.displayName).isEqualTo("disabledTest")
    assertThat(listener.skipped[skipped]).isEqualTo("not ready")
    assertThat(listener.started.map { it.displayName }).doesNotContain("disabledTest")
  }

  @Test
  fun disabledClassIsReportedSkippedNotExecuted() {
    val listener = execute(DisabledClassFixture::class.java)

    assertThat(listener.skipped.keys.map { it.displayName })
      .containsExactly(DisabledClassFixture::class.java.simpleName)
    assertThat(listener.skipped.values.single()).isEqualTo("whole class off")
    assertThat(listener.started.map { it.displayName }).doesNotContain("neverRuns")
  }

  @Test
  fun unsupportedKindsAreDiscoveredAndFailLoudly() {
    val root = discover(UnsupportedKindsFixture::class.java)
    val methodDescriptors = root.descendants.filter { it.isTest }

    assertThat(methodDescriptors.map { it.displayName })
      .containsExactly("parameterized", "sdkTemplate", "factory")

    val listener = execute(UnsupportedKindsFixture::class.java)
    val failures =
      listener.finished
        .filterKeys { it.isTest }
        .values
        .map { result ->
          assertThat(result.status).isEqualTo(TestExecutionResult.Status.FAILED)
          result.throwable.get().message.orEmpty()
        }
    assertThat(failures).hasSize(3)
    failures.forEach { message ->
      assertThat(message).contains("not supported by robolectric-junit-jupiter-engine")
      assertThat(message).contains("@ExtendWith(RobolectricExtension::class)")
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
    val started = mutableListOf<TestDescriptor>()
    val finished = mutableMapOf<TestDescriptor, TestExecutionResult>()
    val skipped = mutableMapOf<TestDescriptor, String>()

    override fun executionStarted(testDescriptor: TestDescriptor) {
      started += testDescriptor
    }

    override fun executionFinished(
      testDescriptor: TestDescriptor,
      testExecutionResult: TestExecutionResult,
    ) {
      finished[testDescriptor] = testExecutionResult
    }

    override fun executionSkipped(testDescriptor: TestDescriptor, reason: String) {
      skipped[testDescriptor] = reason
    }

    override fun dynamicTestRegistered(testDescriptor: TestDescriptor) = Unit

    override fun reportingEntryPublished(testDescriptor: TestDescriptor, entry: ReportEntry) = Unit
  }
}
