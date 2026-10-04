package org.robolectric.kotest.internal

import io.kotest.core.extensions.Extension
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.AbstractSpec
import io.kotest.core.spec.Spec
import io.kotest.core.spec.functionOverrideCallbacks
import io.kotest.core.test.TestCase
import java.util.IdentityHashMap
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

/**
 * The spec that Kotest gets for a spec class, and the instances of the class that it stands in for:
 * one in the Android environment of each SDK that is selected for the spec.
 *
 * Kotest loads a spec class itself, outside of the sandbox, and runs the tests of the one spec that
 * it is given. The instances that can use Android are created in the sandbox, so [spec] has their
 * root tests, is configured as they are, and passes Kotest's callbacks on to them, see [StandIns].
 */
internal class StandIn(val spec: AbstractSpec, val members: List<Member>) {
  /** The root tests of [spec], which are those of the members, with the member of each. */
  val owners: MutableMap<Any, Member> = IdentityHashMap()

  /** Returns the member that the test, or the root test that it is inside of, comes from. */
  fun memberOf(testCase: TestCase): Member? =
    owners[generateSequence(testCase) { it.parent }.last().test]
}

/**
 * An instance that a [StandIn] stands in for, the Android environment that it was created in and
 * runs in, Android's main dispatcher for that environment if the code under test has one, and the
 * callbacks that Kotest would take from the instance.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class Member(
  val spec: Spec,
  val environment: RobolectricEnvironment,
  val mainDispatcher: MainCoroutineDispatcher?,
) {
  val callbacks: List<Extension> =
    spec.extensions + spec.functionOverrideCallbacks() + spec.extensions()

  // How the callbacks before the first test of the instance went, once they ran.
  private var started: Result<Unit>? = null
  private val starting = Mutex()

  /**
   * Runs what the instance wants to run before its first test, the first time. Kotest runs that for
   * a spec, which the instance is not for Kotest.
   */
  suspend fun start() {
    starting
      .withLock {
        started
          ?: runCatching {
            callbacks.filterIsInstance<BeforeSpecListener>().forEach { it.beforeSpec(spec) }
          }
            .also { started = it }
      }
      .getOrThrow()
  }

  /**
   * Closes what the instance opened, and runs what it wants to run after its last test, if a test
   * of it ran, as Kotest does for a spec.
   */
  suspend fun finish() {
    if (started?.isSuccess == true) {
      spec.autoCloseables().forEach { if (it.isInitialized()) it.value.close() }
      callbacks.filterIsInstance<AfterSpecListener>().forEach { it.afterSpec(spec) }
    }
  }
}
