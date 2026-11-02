package org.robolectric.kotest.internal

import io.kotest.core.Tag
import io.kotest.core.TestConfiguration
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.spec.AbstractSpec
import io.kotest.core.spec.Spec
import io.kotest.core.spec.TestDefinition
import io.kotest.engine.tags.tags
import org.robolectric.annotation.Config
import org.robolectric.runner.common.ExperimentalRunnerApi

/** Creates the specs that Kotest gets for the instances of a spec class in the sandbox. */
@OptIn(ExperimentalRunnerApi::class)
internal object StandIns {
  private const val ALWAYS_MARK_SDK = "robolectric.alwaysIncludeVariantMarkersInTestName"

  /**
   * Returns a spec for Kotest that stands in for the instances: a shell of the spec class if it can
   * have one, which has the root tests of the instances, is configured as they are, and whose
   * callbacks are theirs.
   *
   * If the instances are for several SDKs, each root test is marked with its SDK as the test runner
   * marks tests: `name[33]`, and `name` on the last SDK.
   */
  fun create(specClass: Class<out Spec>, instances: List<SpecInstance>): StandIn {
    val first = instances.first().spec
    // With one instance for each root test, Kotest asks for as many specs as there are root tests.
    val further = instances.sumOf { it.spec.tests().size }
    val spec =
      Shells.create(specClass, first, further)
        ?: StandInSpec(first).also { addClassTags(it, first) }
    val standIn = StandIn(spec, instances.map { Member(it) })
    copyConfiguration(first, spec)
    val marksLastSdk = java.lang.Boolean.getBoolean(ALWAYS_MARK_SDK)
    standIn.members
      .flatMap { member ->
        val marksSdk = instances.size > 1 && (marksLastSdk || member !== standIn.members.last())
        member.instance.spec.tests().mapIndexed { index, test ->
          standIn.owners[test.test] = member
          index to if (marksSdk) marked(test, member) else test
        }
      }
      // A test of the real spec runs on one SDK after the other, and then the next test does.
      .sortedBy { (index, _) -> index }
      .forEach { (_, test) -> spec.add(test) }
    for (member in standIn.members) {
      spec.extensions(member.callbacks.map { Callbacks.of(it, member, standIn) })
      afterProjectListeners(spec).addAll(afterProjectListeners(member.instance.spec))
    }
    return standIn
  }

  private fun marked(test: TestDefinition, member: Member): TestDefinition {
    val sdk = member.instance.environment.configuration.get(Config::class.java).sdk.single()
    return test.copy(name = test.name.copy(name = "${test.name.name}[$sdk]"))
  }

  /** Configures a spec as another one is configured with properties, such as its timeout. */
  private fun copyConfiguration(from: Spec, to: AbstractSpec) {
    to.defaultTestConfig = from.defaultTestConfig
    to.isolationMode = from.isolationMode
    to.testOrder = from.testOrder
    to.testCaseOrder = from.testCaseOrder
    to.timeout = from.timeout
    to.invocationTimeout = from.invocationTimeout
    to.assertionMode = from.assertionMode
    to.assertions = from.assertions
    to.assertSoftly = from.assertSoftly
    to.testExecutionMode = from.testExecutionMode
    to.severity = from.severity
    to.failfast = from.failfast
    to.retries = from.retries
    to.retryDelay = from.retryDelay
    to.coroutineDispatcherFactory = from.coroutineDispatcherFactory
    to.blockingTest = from.blockingTest
    to.coroutineTestScope = from.coroutineTestScope
    to.nonDeterministicTestVirtualTimeEnabled = from.nonDeterministicTestVirtualTimeEnabled
    to.coroutineDebugProbes = from.coroutineDebugProbes
    to.duplicateTestNameMode = from.duplicateTestNameMode
    tagsOf(from).forEach { to.tags(it) }
  }

  /** Gives a spec that is not of the spec class the tags that the class is annotated with. */
  private fun addClassTags(spec: AbstractSpec, real: Spec) {
    real::class.tags(false).forEach { spec.tags(it) }
  }

  // Kotest keeps the tags that a spec adds to itself with tags(...), and what it wants to run
  // after the project, in fields that only Kotest reads. A spec that stands in for another has to
  // have them too, so they are read from those fields. If Kotest renames one, they are left out.

  private fun tagsOf(spec: Spec): Set<Tag> =
    field(TestConfiguration::class.java, "_tags", spec) ?: emptySet()

  private fun afterProjectListeners(spec: Spec): MutableList<AfterProjectListener> =
    field(Spec::class.java, "afterProjectListeners", spec) ?: mutableListOf()

  @Suppress("UNCHECKED_CAST")
  private fun <T> field(owner: Class<*>, name: String, spec: Spec): T? =
    try {
      owner.getDeclaredField(name).apply { isAccessible = true }.get(spec) as T
    } catch (_: NoSuchFieldException) {
      null
    }
}
