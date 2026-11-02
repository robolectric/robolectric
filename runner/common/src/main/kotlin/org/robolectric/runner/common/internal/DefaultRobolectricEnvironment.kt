package org.robolectric.runner.common.internal

import java.time.Duration
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import org.robolectric.internal.AndroidSandbox
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

@OptIn(ExperimentalRunnerApi::class)
internal class DefaultRobolectricEnvironment(
  private val session: DefaultRobolectricSession,
  override val configuration: EnvironmentConfiguration,
  private val sandbox: AndroidSandbox,
) : RobolectricEnvironment {
  private val mainThread: Thread = sandbox.runOnMainThread(Callable { Thread.currentThread() })
  private val closed = AtomicBoolean()

  override val classLoader: ClassLoader
    get() = sandbox.robolectricClassLoader

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

  override fun <T> post(action: Callable<T>): T {
    check(!closed.get()) { "The environment for $configuration is closed" }
    if (Thread.currentThread() === mainThread) {
      return withSandboxClassLoader(action)
    }
    val result = CompletableFuture<T>()
    val task = Runnable {
      runCatching { withSandboxClassLoader(action) }
        .fold(result::complete, result::completeExceptionally)
    }
    // Android's classes are those of the sandbox: Handler(Looper.getMainLooper()).post(task).
    val looper = classLoader.loadClass("android.os.Looper")
    val mainLooper = looper.getMethod("getMainLooper").invoke(null)
    val handler = classLoader.loadClass("android.os.Handler").getConstructor(looper)
    handler.declaringClass
      .getMethod("post", Runnable::class.java)
      .invoke(handler.newInstance(mainLooper), task)
    try {
      return result.get()
    } catch (e: ExecutionException) {
      throw e.cause ?: e
    }
  }

  override fun loadClass(original: Class<*>): Class<*> =
    if (original.isPrimitive) original else Class.forName(original.name, false, classLoader)

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
      session.onClosed(this, sandbox, Duration.ofNanos(System.nanoTime() - start))
    }
  }

  private fun <T> onMainThread(action: Callable<T>): T =
    if (Thread.currentThread() === mainThread) {
      withSandboxClassLoader(action)
    } else {
      sandbox.runOnMainThread(Callable { withSandboxClassLoader(action) })
    }

  private fun <T> withSandboxClassLoader(action: Callable<T>): T {
    val thread = Thread.currentThread()
    val prior = thread.contextClassLoader
    thread.contextClassLoader = classLoader
    try {
      return action.call()
    } finally {
      thread.contextClassLoader = prior
    }
  }
}
