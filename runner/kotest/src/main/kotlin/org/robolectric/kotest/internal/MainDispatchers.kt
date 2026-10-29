package org.robolectric.kotest.internal

import java.util.concurrent.Callable
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

/**
 * Gives what runs in an Android environment the main dispatcher of that environment as
 * `Dispatchers.Main`.
 *
 * The sandboxes share `kotlinx.coroutines` with Kotest, so that the scope of a test, which Kotest
 * creates, is one for the code of the test too. `Dispatchers.Main` then is one for the whole JVM,
 * and not what Android code expects: the dispatcher of the main looper of its sandbox. So before
 * something of a spec runs, this sets `Dispatchers.Main` to the dispatcher of the environment that
 * it runs in, unless a test set a main dispatcher of its own.
 */
@OptIn(ExperimentalRunnerApi::class, ExperimentalCoroutinesApi::class)
internal object MainDispatchers {
  // The class that kotlinx-coroutines-android creates Android's main dispatcher with, which
  // kotlinx.coroutines itself looks up by this name.
  private const val FACTORY = "kotlinx.coroutines.android.AndroidDispatcherFactory"
  private val lock = Any()

  // Guarded by the lock: the dispatcher of an environment that is set as Dispatchers.Main.
  private var current: MainCoroutineDispatcher? = null

  /**
   * Returns Android's main dispatcher for the environment, as the sandbox creates it, or null if
   * the code under test doesn't have kotlinx-coroutines-android.
   */
  fun create(environment: RobolectricEnvironment): MainCoroutineDispatcher? =
    environment.run(
      Callable {
        try {
          val factory = environment.classLoader.loadClass(FACTORY)
          factory
            .getMethod("createDispatcher", List::class.java)
            .invoke(factory.getDeclaredConstructor().newInstance(), emptyList<Any>())
            as MainCoroutineDispatcher
        } catch (_: ClassNotFoundException) {
          null
        }
      }
    )

  /**
   * Sets `Dispatchers.Main` to the dispatcher, if there is none, also because a test reset the one
   * that it had set, or if it is that of another environment. One that a test set stays.
   */
  fun use(dispatcher: MainCoroutineDispatcher?) {
    synchronized(lock) {
      val replaces = isMissing() || dispatcher !== current && isCurrentSet()
      if (dispatcher != null && replaces) {
        Dispatchers.setMain(dispatcher)
        current = dispatcher
      }
    }
  }

  /** Makes the dispatcher, of an environment that closes, no longer be `Dispatchers.Main`. */
  fun release(dispatcher: MainCoroutineDispatcher?) {
    synchronized(lock) {
      if (dispatcher != null && dispatcher === current) {
        if (isCurrentSet()) {
          Dispatchers.resetMain()
        }
        current = null
      }
    }
  }

  private fun isMissing(): Boolean = runCatching {
    Dispatchers.Main.isDispatchNeeded(EmptyCoroutineContext)
  }
    .isFailure

  /** Returns whether `Dispatchers.Main` still is what this set it to. */
  private fun isCurrentSet(): Boolean =
    current != null && runCatching { Dispatchers.Main.immediate }.getOrNull() === current?.immediate
}
