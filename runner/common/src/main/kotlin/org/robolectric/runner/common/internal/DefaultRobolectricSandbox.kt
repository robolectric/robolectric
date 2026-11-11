package org.robolectric.runner.common.internal

import java.time.Duration
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.robolectric.internal.AndroidSandbox
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment
import org.robolectric.runner.common.RobolectricSandbox
import org.robolectric.runner.common.SandboxKey

/** A sandbox that a session lent out, in which one environment at a time is open. */
@OptIn(ExperimentalRunnerApi::class)
internal class DefaultRobolectricSandbox(
  private val session: DefaultRobolectricSession,
  override val key: SandboxKey,
  val sandbox: AndroidSandbox,
  created: Boolean,
) : RobolectricSandbox {
  /** The thread that Android's main looper runs on in the sandbox. */
  val mainThread: Thread = sandbox.runOnMainThread(Callable { Thread.currentThread() })
  private val closed = AtomicBoolean()
  private val environment = AtomicReference<DefaultRobolectricEnvironment?>()

  // Whether the sandbox was created for this lease, until an environment is opened in it.
  private val newlyCreated = AtomicBoolean(created)

  override val classLoader: ClassLoader
    get() = sandbox.robolectricClassLoader

  override fun <T> run(action: Callable<T>): T {
    check(!closed.get()) { "The sandbox for $key is closed" }
    return onMainThread(action)
  }

  override fun loadClass(original: Class<*>): Class<*> =
    if (original.isPrimitive) original else Class.forName(original.name, false, classLoader)

  override fun prepare(configuration: Configuration) {
    check(!closed.get()) { "The sandbox for $key is closed" }
    val fitting = fitting(configuration)
    claim(sandbox)
    try {
      session.setup.configure(sandbox, fitting)
    } finally {
      free(sandbox)
    }
  }

  override fun open(configuration: Configuration): RobolectricEnvironment =
    open(fitting(configuration), ownsLease = false, System.nanoTime())

  /** Sets up an environment, which gives back the lease when it closes if it [ownsLease]. */
  fun open(
    configuration: EnvironmentConfiguration,
    ownsLease: Boolean,
    start: Long,
  ): RobolectricEnvironment {
    check(!closed.get()) { "The sandbox for $key is closed" }
    claim(sandbox)
    val opened =
      try {
        // Configure the sandbox before its class loader is used, as the test runner does.
        session.setup.configure(sandbox, configuration)
        DefaultRobolectricEnvironment(session, configuration, this, ownsLease).also {
          environment.set(it)
          it.setUp()
        }
      } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
        environment.set(null)
        free(sandbox)
        throw e
      }
    val duration = Duration.ofNanos(System.nanoTime() - start)
    session.listener?.environmentOpened(configuration, newlyCreated.getAndSet(false), duration)
    return opened
  }

  /** Called by an environment of the lease once it is closed. */
  fun environmentClosed(closed: DefaultRobolectricEnvironment, duration: Duration) {
    environment.compareAndSet(closed, null)
    free(sandbox)
    if (closed.ownsLease) {
      close()
    }
    session.listener?.environmentClosed(closed.configuration, duration)
  }

  /** Closes the environment that is open in the sandbox, if there is one, and gives it back. */
  override fun close() {
    if (closed.compareAndSet(false, true)) {
      try {
        environment.get()?.close()
      } finally {
        session.release(this)
      }
    }
  }

  /** Runs the action on the main thread, with the sandbox's class loader as the context's. */
  fun <T> onMainThread(action: Callable<T>): T =
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

  private fun fitting(configuration: Configuration): EnvironmentConfiguration {
    val fits = session.sandboxKey(configuration)
    require(fits == key) { "$configuration needs a sandbox for $fits, not for $key" }
    return configuration as EnvironmentConfiguration
  }

  private companion object {
    // The sandboxes of the JVM that an environment is open in, or that are being configured. Leases
    // of sessions that share sandboxes can be for the same one.
    private val busy: MutableSet<AndroidSandbox> = Collections.newSetFromMap(IdentityHashMap())

    private fun claim(sandbox: AndroidSandbox) {
      check(synchronized(busy) { busy.add(sandbox) }) {
        "An environment is open in the sandbox, or it is being configured"
      }
    }

    private fun free(sandbox: AndroidSandbox) {
      synchronized(busy) { busy.remove(sandbox) }
    }
  }
}
