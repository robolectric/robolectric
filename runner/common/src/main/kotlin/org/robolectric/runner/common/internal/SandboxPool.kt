package org.robolectric.runner.common.internal

import java.util.IdentityHashMap
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.ResourcesMode
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.SandboxManager
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.pluginapi.Sdk
import org.robolectric.plugins.SdkCollection
import org.robolectric.util.inject.Injector

/** What makes a sandbox fit an environment, as for the test runner's sandbox manager. */
internal data class SandboxIdentity(
  val instrumentation: InstrumentationConfiguration,
  val sdk: Sdk,
  val resourcesMode: ResourcesMode.Mode,
  val looperMode: LooperMode.Mode,
  val graphicsMode: GraphicsMode.Mode,
) {
  /** Creates a sandbox for the identity, as the test runner's sandbox manager does. */
  fun build(builder: SandboxManager.SandboxBuilder, sdks: SdkCollection): AndroidSandbox =
    builder.build(instrumentation, sdk, sdks.maxSupportedSdk, resourcesMode)
}

/** Lends out the sandboxes of a session. */
internal interface Sandboxes {
  /** A sandbox that is lent out, and whether it was created for that. */
  class Lease(val sandbox: AndroidSandbox, val created: Boolean)

  fun acquire(identity: SandboxIdentity): Lease

  fun release(sandbox: AndroidSandbox)

  /** Gives back what is lent out, and shuts down the sandboxes that are the session's own. */
  fun shutdown()
}

/**
 * Lends out sandboxes, each to one environment at a time. A sandbox is reused once it is returned,
 * and another one is created if all that fit are lent out, so environments never wait for each
 * other. As the test runner's sandbox manager does, it keeps a limited number of sandboxes.
 */
internal class SandboxPool(injector: Injector) : Sandboxes {
  private val builder = injector.getInstance(SandboxManager.SandboxBuilder::class.java)
  private val sdks = injector.getInstance(SdkCollection::class.java)
  private val maxSize = sdks.supportedSdks.size * SANDBOXES_PER_SDK

  // The sandboxes of the pool, and those that aren't lent out, least recently used first.
  private val keys = IdentityHashMap<AndroidSandbox, SandboxIdentity>()
  private val idle = ArrayDeque<AndroidSandbox>()

  override fun acquire(identity: SandboxIdentity): Sandboxes.Lease {
    val reused =
      synchronized(this) {
        idle.lastOrNull { keys[it] == identity && !it.isShutdown }?.also { idle.remove(it) }
      }
    if (reused != null) {
      return Sandboxes.Lease(reused, created = false)
    }
    val sandbox = identity.build(builder, sdks)
    synchronized(this) { keys[sandbox] = identity }
    return Sandboxes.Lease(sandbox, created = true)
  }

  override fun release(sandbox: AndroidSandbox) {
    val evicted = mutableListOf<AndroidSandbox>()
    synchronized(this) {
      if (sandbox !in keys) return
      idle.addLast(sandbox)
      while (keys.size > maxSize && idle.isNotEmpty()) {
        val eldest = idle.removeFirst()
        keys.remove(eldest)
        evicted.add(eldest)
      }
    }
    evicted.forEach { it.shutdown() }
  }

  override fun shutdown() {
    val sandboxes =
      synchronized(this) {
        keys.keys.toList().also {
          keys.clear()
          idle.clear()
        }
      }
    sandboxes.filterNot { it.isShutdown }.forEach { it.shutdown() }
  }

  companion object {
    // Tests of one SDK can need differently configured sandboxes.
    const val SANDBOXES_PER_SDK = 3
  }
}
