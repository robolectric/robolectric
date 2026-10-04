package org.robolectric.runner.common.internal

import java.time.Duration
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import org.robolectric.internal.AndroidSandbox
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

@OptIn(ExperimentalRunnerApi::class)
internal class DefaultRobolectricEnvironment(
  private val session: DefaultRobolectricSession,
  override val configuration: EnvironmentConfiguration,
  /** The lease of the sandbox that the environment is set up in. */
  val lease: DefaultRobolectricSandbox,
  /** Whether closing the environment gives back the lease, which it has to itself. */
  val ownsLease: Boolean,
) : RobolectricEnvironment {
  private val sandbox: AndroidSandbox = lease.sandbox
  private val closed = AtomicBoolean()

  override val classLoader: ClassLoader
    get() = lease.classLoader

  /** The default locale that Android set for the JVM when it set up the application state. */
  var locale: Locale? = null
    private set

  /** Sets up the application state, and resets the sandbox if that fails. */
  fun setUp() {
    synchronized(session.applicationStateLock) {
      session.beforeSetUp()
      try {
        onMainThread {
          sandbox.testEnvironment.setUpApplicationState(
            configuration.name,
            configuration,
            configuration.manifest,
          )
        }
        locale = Locale.getDefault()
        session.afterSetUp(this)
      } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        try {
          onMainThread { sandbox.testEnvironment.resetState() }
        } catch (@Suppress("TooGenericExceptionCaught") resetFailure: Throwable) {
          e.addSuppressed(resetFailure)
        }
        session.afterTearDown(this)
        throw e
      }
    }
  }

  override fun <T> run(action: Callable<T>): T {
    check(!closed.get()) { "The environment for $configuration is closed" }
    return onMainThread(action)
  }

  override fun loadClass(original: Class<*>): Class<*> = lease.loadClass(original)

  override fun diagnoseFailure(failure: Throwable) {
    if (!closed.get()) {
      onMainThread { sandbox.testEnvironment.checkStateAfterTestFailure(failure) }
    }
  }

  override fun close() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    val start = System.nanoTime()
    try {
      synchronized(session.applicationStateLock) {
        try {
          onMainThread {
            try {
              sandbox.testEnvironment.tearDownApplication()
            } finally {
              // An interrupt left over from the code that ran here would break later environments.
              Thread.interrupted()
              sandbox.testEnvironment.resetState()
            }
          }
        } finally {
          session.afterTearDown(this)
        }
      }
    } finally {
      lease.environmentClosed(this, Duration.ofNanos(System.nanoTime() - start))
    }
  }

  private fun <T> onMainThread(action: Callable<T>): T = lease.onMainThread(action)
}
