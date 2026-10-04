package org.robolectric.kotest.internal

import io.kotest.core.spec.AbstractSpec
import io.kotest.core.spec.Spec

/**
 * Stands in for the instances of a spec class that can't have a shell, see [Shells]. Kotest calls
 * the functions that a spec configures itself with on the spec that it got, which is this one, so
 * they return what an instance of the real spec returns.
 */
internal class StandInSpec(private val real: Spec) : AbstractSpec() {
  override fun isolationMode() = real.isolationMode()

  override fun testCaseOrder() = real.testCaseOrder()

  override fun timeout() = real.timeout()

  override fun invocationTimeout() = real.invocationTimeout()

  override fun tags() = real.tags()

  override fun assertionMode() = real.assertionMode()

  override fun testExecutionMode() = real.testExecutionMode()

  override fun coroutineDispatcherFactory() = real.coroutineDispatcherFactory()

  override fun duplicateTestNameMode() = real.duplicateTestNameMode()
}
