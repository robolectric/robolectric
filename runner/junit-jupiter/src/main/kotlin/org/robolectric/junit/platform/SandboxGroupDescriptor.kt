@file:OptIn(org.robolectric.runner.common.ExperimentalRunnerApi::class)

package org.robolectric.junit.platform

import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.RobolectricSandbox
import org.robolectric.runner.common.RobolectricSession

internal class SandboxGroupDescriptor(
  id: UniqueId,
  sdk: Int,
  private val representative: Configuration,
) : AbstractTestDescriptor(id, "SDK $sdk") {
  private val configurations = linkedMapOf<UniqueId, Configuration>()
  private val suffixes = linkedMapOf<UniqueId, String>()
  private var selectedIds: List<UniqueId> = emptyList()

  override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER

  fun restrictTo(ids: List<UniqueId>) {
    selectedIds = ids
  }

  fun add(
    plan: TestPlan,
    identifier: TestIdentifier,
    configuration: Configuration,
    suffix: String,
  ) {
    configurations[identifier.uniqueIdObject] = configuration
    suffixes[identifier.uniqueIdObject] = suffix
    addMirror(plan, identifier, suffix)
  }

  private fun addMirror(
    plan: TestPlan,
    identifier: TestIdentifier,
    suffix: String,
  ): TestDescriptor {
    val id = JupiterDelegate.outer(identifier.uniqueIdObject, uniqueId)
    findByUniqueId(id).orElse(null)?.let {
      return it
    }
    val parentIdentifier = plan.getParent(identifier).orElse(null)
    val parent =
      if (parentIdentifier == null || !parentIdentifier.parentId.isPresent) this
      else addMirror(plan, parentIdentifier, "")
    return MirrorDescriptor(id, identifier, suffix).also(parent::addChild)
  }

  @Suppress("TooGenericExceptionCaught")
  fun execute(request: ExecutionRequest, session: RobolectricSession) {
    val listener = request.engineExecutionListener
    listener.executionStarted(this)
    try {
      val sandbox = ProcessSandbox.acquire(representative)
      for (child in children.toList()) {
        executeClass(request, session, sandbox, child)
      }
      listener.executionFinished(this, TestExecutionResult.successful())
    } catch (failure: Throwable) {
      listener.executionFinished(this, TestExecutionResult.failed(failure))
    }
  }

  @Suppress("TooGenericExceptionCaught")
  private fun executeClass(
    request: ExecutionRequest,
    session: RobolectricSession,
    sandbox: RobolectricSandbox,
    child: TestDescriptor,
  ) {
    val leaves = mutableListOf<TestDescriptor>()
    collectLeaves(child, leaves)
    if (leaves.isEmpty()) return
    val selected = leaves.flatMap { leaf ->
      val descendants = selectedIds.filter { it.hasPrefix(leaf.uniqueId) }
      (descendants.ifEmpty { listOf(leaf.uniqueId) }).map { JupiterDelegate.inner(it, uniqueId) }
    }
    val classConfigurations = configurations.filterKeys { id ->
      selected.any { it.hasPrefix(id) || id.hasPrefix(it) }
    }
    val first = classConfigurations.values.firstOrNull() ?: representative
    val testClass = (child.source.orElse(null) as? ClassSource)?.getJavaClass()
    val forwarding = forwarding(request.engineExecutionListener, child)
    var active: ActiveSandbox? = null
    try {
      val classConfiguration =
        testClass
          ?.let { session.plan(it, null) }
          ?.firstOrNull { session.sandboxKey(it).sdk == sandbox.key.sdk }
          ?.also(ProcessSandbox::validate)
          ?.takeIf { session.sandboxKey(it) == sandbox.key } ?: first
      active = ActiveSandbox(sandbox, classConfigurations, classConfiguration)
      // Register first, so that a concurrent launcher fails without reconfiguring the sandbox.
      ActiveSandboxes.register(active)
      sandbox.prepare(first)
      sandbox.run {
        JupiterDelegate(request.configurationParameters)
          .execute(selected.map { DiscoverySelectors.selectUniqueId(it) }, forwarding)
      }
      forwarding.finish()
    } catch (failure: Throwable) {
      forwarding.fail(failure)
    } finally {
      active?.let(ActiveSandboxes::remove)
    }
  }

  private fun forwarding(listener: EngineExecutionListener, classDescriptor: TestDescriptor) =
    object : TestExecutionListener {
      private val mirrors = linkedMapOf<UniqueId, TestDescriptor>()
      private val reported = mutableSetOf<UniqueId>()
      private val succeeded = mutableSetOf<UniqueId>()
      private val finished = mutableSetOf<UniqueId>()
      private var rootFailure: Throwable? = null

      init {
        collect(classDescriptor)
      }

      private fun collect(descriptor: TestDescriptor) {
        mirrors[descriptor.uniqueId] = descriptor
        descriptor.children.forEach(::collect)
      }

      private fun mirror(identifier: TestIdentifier): TestDescriptor {
        val id = JupiterDelegate.outer(identifier.uniqueIdObject, uniqueId)
        return mirrors.getOrPut(id) {
          val parent =
            identifier.parentIdObject.orElse(null)?.let {
              mirrors[JupiterDelegate.outer(it, uniqueId)]
            } ?: this@SandboxGroupDescriptor
          val suffix =
            suffixes.entries
              .filter { identifier.uniqueIdObject.hasPrefix(it.key) }
              .maxByOrNull { it.key.segments.size }
              ?.value ?: ""
          MirrorDescriptor(id, identifier, suffix).also {
            parent.addChild(it)
            listener.dynamicTestRegistered(it)
          }
        }
      }

      override fun dynamicTestRegistered(identifier: TestIdentifier) {
        mirror(identifier)
      }

      override fun executionStarted(identifier: TestIdentifier) {
        if (identifier.parentId.isPresent) {
          val descriptor = mirror(identifier)
          reported.add(descriptor.uniqueId)
          listener.executionStarted(descriptor)
        }
      }

      override fun executionSkipped(identifier: TestIdentifier, reason: String) {
        if (identifier.parentId.isPresent) {
          val descriptor = mirror(identifier)
          reported.add(descriptor.uniqueId)
          listener.executionSkipped(descriptor, reason)
        }
      }

      override fun executionFinished(identifier: TestIdentifier, result: TestExecutionResult) {
        if (identifier.parentId.isPresent) {
          val descriptor = mirror(identifier)
          finished.add(descriptor.uniqueId)
          if (result.status == TestExecutionResult.Status.SUCCESSFUL)
            succeeded.add(descriptor.uniqueId)
          listener.executionFinished(descriptor, result)
        } else {
          rootFailure = result.throwable.orElse(null)
        }
      }

      fun finish() {
        rootFailure?.let { throw it }
        reportMissing(classDescriptor)
      }

      fun fail(failure: Throwable) {
        if (classDescriptor.uniqueId in finished) throw failure
        if (reported.add(classDescriptor.uniqueId)) listener.executionStarted(classDescriptor)
        listener.executionFinished(classDescriptor, TestExecutionResult.failed(failure))
      }

      private fun reportMissing(descriptor: TestDescriptor) {
        if (descriptor.uniqueId !in reported) {
          listener.executionStarted(descriptor)
          listener.executionFinished(
            descriptor,
            TestExecutionResult.failed(
              IllegalStateException(
                "${descriptor.uniqueId} was not found when running in the sandbox",
              ),
            ),
          )
        } else if (descriptor.uniqueId in succeeded) {
          descriptor.children.toList().forEach(::reportMissing)
        }
      }

      override fun reportingEntryPublished(identifier: TestIdentifier, entry: ReportEntry) {
        listener.reportingEntryPublished(
          if (identifier.parentId.isPresent) mirror(identifier) else this@SandboxGroupDescriptor,
          entry,
        )
      }
    }

  private fun collectLeaves(descriptor: TestDescriptor, leaves: MutableList<TestDescriptor>) {
    if (descriptor.children.isEmpty()) leaves.add(descriptor)
    else descriptor.children.forEach { collectLeaves(it, leaves) }
  }
}
