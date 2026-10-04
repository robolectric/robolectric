@file:OptIn(org.robolectric.runner.common.ExperimentalRunnerApi::class)

package org.robolectric.junit.platform

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import org.junit.jupiter.api.Disabled
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.engine.DiscoveryFilter
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.discovery.UniqueIdSelector
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.robolectric.annotation.Config
import org.robolectric.runner.common.RobolectricSession
import org.robolectric.runner.common.SandboxKey

/** Runs Jupiter inside one Android sandbox for this JVM. */
public class RobolectricTestEngine : TestEngine {
  override fun getId(): String = "robolectric"

  override fun discover(request: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
    val session = ProcessSandbox.createSession()
    val root = PlannedEngine(uniqueId, session)
    val delegate = JupiterDelegate(request.configurationParameters)
    val uniqueIds = request.getSelectorsByType(UniqueIdSelector::class.java).map { it.uniqueId }
    val selectors =
      request.getSelectorsByType(DiscoverySelector::class.java).mapNotNull {
        if (it is UniqueIdSelector) {
          if (!it.uniqueId.hasPrefix(uniqueId)) null
          else
            DiscoverySelectors.selectUniqueId(
              it.uniqueId.segments.drop(2).fold(UniqueId.forEngine("junit-jupiter")) { id, segment
                ->
                id.append(segment.type, segment.value)
              },
            )
        } else it
      }
    val plan = delegate.discover(selectors, request.getFiltersByType(DiscoveryFilter::class.java))
    val groups = linkedMapOf<SandboxKey, SandboxGroupDescriptor>()
    for (identifier in plan.roots.flatMap { plan.getDescendants(it) }) {
      val source = identifier.source.orElse(null) as? MethodSource ?: continue
      val testClass = configurationClass(plan, identifier) ?: source.getJavaClass()
      if (RobolectricClaimFilter.usesRobolectric(testClass)) {
        Claims.claim(topLevel(testClass))
        addConfigurations(root, groups, plan, identifier, testClass, source)
      }
    }
    if (uniqueIds.isNotEmpty()) {
      prune(root, uniqueIds)
      groups.values.forEach { it.restrictTo(uniqueIds) }
    }
    Claims.complete()
    return root
  }

  private fun addConfigurations(
    root: PlannedEngine,
    groups: MutableMap<SandboxKey, SandboxGroupDescriptor>,
    plan: TestPlan,
    identifier: TestIdentifier,
    testClass: Class<*>,
    source: MethodSource,
  ) {
    val id = JupiterDelegate.outer(identifier.uniqueIdObject, root.uniqueId)
    try {
      val configurations = root.session.plan(testClass, source.getJavaMethod())
      require(configurations.size <= 1) {
        "The robolectric JUnit Platform engine uses one sandbox per JVM. " +
          "Select one SDK with robolectric.enabledSdks and use separate test tasks or JVM forks " +
          "to run other SDKs."
      }
      if (configurations.isEmpty()) {
        root.addChild(
          UnavailableTest(id, identifier, null, noSdkReason(testClass, source.getMethodName())),
        )
      }
      for (configuration in configurations) {
        ProcessSandbox.validate(configuration)
        val key = root.session.sandboxKey(configuration)
        require(groups.isEmpty() || key in groups) {
          "The robolectric JUnit Platform engine uses one sandbox per JVM. " +
            "Use separate test tasks or JVM forks for different SDKs, looper modes, or instrumentation."
        }
        val group =
          groups.getOrPut(key) {
            SandboxGroupDescriptor(root.uniqueId.append("sdk", key.id), key.sdk, configuration)
              .also(root::addChild)
          }
        val suffix =
          if (java.lang.Boolean.getBoolean("robolectric.alwaysIncludeVariantMarkersInTestName")) {
            "[${configuration.get(Config::class.java).sdk.single()}]"
          } else ""
        group.add(plan, identifier, configuration, suffix)
      }
    } catch (failure: IllegalArgumentException) {
      root.addChild(
        if (isDisabled(testClass, source.getJavaMethod())) {
          UnavailableTest(id, identifier, null, "Disabled: ${failure.message}")
        } else {
          UnavailableTest(id, identifier, failure)
        },
      )
    }
  }

  @Suppress("TooGenericExceptionCaught")
  override fun execute(request: ExecutionRequest) {
    val listener = request.engineExecutionListener
    val root = request.rootTestDescriptor as PlannedEngine
    listener.executionStarted(root)
    try {
      root.session.use { session ->
        for (child in root.children) {
          when (child) {
            is SandboxGroupDescriptor -> child.execute(request, session)
            is UnavailableTest -> child.execute(listener)
          }
        }
      }
      listener.executionFinished(root, TestExecutionResult.successful())
    } catch (failure: Throwable) {
      listener.executionFinished(root, TestExecutionResult.failed(failure))
    }
  }

  private class PlannedEngine(id: UniqueId, val session: RobolectricSession) :
    EngineDescriptor(id, "Robolectric")

  private class UnavailableTest(
    id: UniqueId,
    identifier: TestIdentifier,
    val failure: Throwable?,
    val reason: String = "",
  ) :
    org.junit.platform.engine.support.descriptor.AbstractTestDescriptor(
      id,
      identifier.displayName,
      identifier.source.orElse(null),
    ) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST

    fun execute(listener: EngineExecutionListener) {
      if (failure == null) {
        listener.executionSkipped(this, reason)
      } else {
        listener.executionStarted(this)
        listener.executionFinished(this, TestExecutionResult.failed(failure))
      }
    }
  }

  private companion object {
    fun noSdkReason(type: Class<*>, method: String): String {
      val enabled = System.getProperty("robolectric.enabledSdks")
      val subject = "${type.name}.$method"
      return if (enabled.isNullOrBlank()) {
        "None of the Android SDKs that $subject is configured for can run on this JVM"
      } else {
        "None of the Android SDKs that $subject is configured for is enabled by " +
          "robolectric.enabledSdks=$enabled"
      }
    }

    fun configurationClass(plan: TestPlan, identifier: TestIdentifier): Class<*>? {
      var current = identifier
      while (true) {
        val source = current.source.orElse(null)
        if (source is ClassSource) return source.getJavaClass()
        current = plan.getParent(current).orElse(null) ?: return null
      }
    }

    /** Whether Jupiter would skip a test, so that its rejected configuration doesn't matter. */
    fun isDisabled(type: Class<*>, method: Method): Boolean =
      AnnotationSupport.isAnnotated(method, Disabled::class.java) ||
        generateSequence(type) { it.enclosingClass }
          .any { AnnotationSupport.isAnnotated(it, Disabled::class.java) }

    fun topLevel(type: Class<*>): Class<*> =
      if (type.enclosingClass != null && !Modifier.isStatic(type.modifiers)) {
        topLevel(type.enclosingClass)
      } else type

    fun prune(descriptor: TestDescriptor, selected: List<UniqueId>) {
      for (child in descriptor.children.toList()) {
        if (selected.none { it.hasPrefix(child.uniqueId) || child.uniqueId.hasPrefix(it) }) {
          child.removeFromHierarchy()
        } else {
          prune(child, selected)
        }
      }
    }
  }
}
