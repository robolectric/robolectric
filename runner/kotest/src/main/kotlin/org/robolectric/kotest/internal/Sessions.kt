package org.robolectric.kotest.internal

import io.kotest.core.spec.AbstractSpec
import io.kotest.core.spec.Spec
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricSession

/**
 * Keeps the session that the specs of a test run open their Android environments in. It is created
 * for the first spec that needs it, and closed when Kotest's project is over.
 */
@OptIn(ExperimentalRunnerApi::class)
internal object Sessions {
  private const val COROUTINES = "kotlinx.coroutines."

  // The packages of kotlinx-coroutines-core, -debug and -test, apart from kotlinx.coroutines.
  private val COROUTINES_PACKAGES =
    listOf(
      "channels",
      "debug",
      "flow",
      "future",
      "internal",
      "intrinsics",
      "scheduling",
      "selects",
      "stream",
      "sync",
      "test",
      "time",
    )
  private val lock = Any()

  // Guarded by the lock: the session, how many specs use it, and whether it is closed once
  // Kotest's project is over.
  private var session: RobolectricSession? = null
  private var users = 0
  private var closesAfterProject = false

  /** Returns the session for a spec to use, until the spec [release]s it. */
  fun acquire(): RobolectricSession =
    synchronized(lock) {
      val current = session ?: create().also { session = it }
      users++
      current
    }

  /**
   * Creates a session whose sandboxes share with Kotest what the specs that are created in them use
   * of Kotest: its own classes, and those of `kotlinx.coroutines` that Kotest runs tests with, so
   * that the scope of a test is a coroutine scope for the code of the test too.
   */
  private fun create(): RobolectricSession {
    val builder = RobolectricSession.builder().sharePackage("io.kotest.")
    // The classes of the package kotlinx.coroutines itself, which start with a capital letter,
    // but not its packages for platforms, such as kotlinx.coroutines.android: those use the
    // classes of the platform as the sandbox loads them.
    for (initial in 'A'..'Z') {
      builder.sharePackage("$COROUTINES$initial")
    }
    COROUTINES_PACKAGES.forEach { builder.sharePackage("$COROUTINES$it.") }
    return builder.build()
  }

  fun release() {
    synchronized(lock) { users-- }
  }

  /**
   * Makes the session be closed when Kotest's project is over. Only a spec can register for that
   * while the project runs, so the first one that can does it for the session.
   */
  fun closeAfterProjectOf(spec: Spec) {
    val registers =
      synchronized(lock) {
        (spec is AbstractSpec && !closesAfterProject).also { if (it) closesAfterProject = true }
      }
    if (registers) {
      (spec as AbstractSpec).afterProject {
        closeIfIdle()
        Unit
      }
    }
  }

  /**
   * Closes the session, unless a spec uses it. The next spec then gets a new one. Returns whether
   * there was a session to close.
   */
  fun closeIfIdle(): Boolean {
    val idle =
      synchronized(lock) {
        if (users > 0) {
          null
        } else {
          session.also {
            session = null
            closesAfterProject = false
          }
        }
      }
    idle?.close()
    return idle != null
  }
}
