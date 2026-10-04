package org.robolectric.runner.common.internal

import java.util.IdentityHashMap
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.ResourcesMode
import org.robolectric.annotation.SQLiteMode
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.SandboxManager
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.pluginapi.Sdk
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.plugins.SdkCollection
import org.robolectric.util.inject.Injector

/**
 * Lends out sandboxes, each to one environment at a time. A sandbox is reused once it is returned,
 * and another one is created if all that fit are lent out, so environments never wait for each
 * other. As the test runner's sandbox manager does, it keeps a limited number of sandboxes.
 */
internal class SandboxPool(injector: Injector) {
  /** What makes a sandbox fit an environment. */
  private data class Key(
    val instrumentation: InstrumentationConfiguration,
    val sdk: Sdk,
    val resourcesMode: ResourcesMode.Mode,
    val looperMode: LooperMode.Mode,
    val graphicsMode: GraphicsMode.Mode,
  )

  private class Idle(val key: Key, val sandbox: AndroidSandbox)

  /** A sandbox that is lent out, and whether it was created for that. */
  class Lease(val sandbox: AndroidSandbox, val created: Boolean)

  private val builder = injector.getInstance(SandboxManager.SandboxBuilder::class.java)
  private val sdks = injector.getInstance(SdkCollection::class.java)
  private val maxSize = sdks.supportedSdks.size * SANDBOXES_PER_SDK

  // The sandboxes of the pool, and those that aren't lent out, least recently used first.
  private val keys = IdentityHashMap<AndroidSandbox, Key>()
  private val idle = ArrayDeque<Idle>()

  fun acquire(
    instrumentation: InstrumentationConfiguration,
    sdk: Sdk,
    configuration: Configuration,
  ): Lease {
    val key =
      Key(
        instrumentation,
        sdk,
        configuration.get(ResourcesMode.Mode::class.java),
        configuration.get(LooperMode.Mode::class.java),
        configuration.get(GraphicsMode.Mode::class.java),
      )
    val sqliteMode = configuration.get(SQLiteMode.Mode::class.java)
    val reused =
      synchronized(this) {
        idle.lastOrNull { it.key == key && !it.sandbox.isShutdown }?.also { idle.remove(it) }
      }
    if (reused != null) {
      reused.sandbox.updateModes(sqliteMode)
      return Lease(reused.sandbox, created = false)
    }
    val sandbox =
      builder.build(instrumentation, sdk, sdks.maxSupportedSdk, key.resourcesMode, sqliteMode)
    synchronized(this) { keys[sandbox] = key }
    return Lease(sandbox, created = true)
  }

  fun release(sandbox: AndroidSandbox) {
    val evicted = mutableListOf<AndroidSandbox>()
    synchronized(this) {
      val key = keys[sandbox] ?: return
      idle.addLast(Idle(key, sandbox))
      while (keys.size > maxSize && idle.isNotEmpty()) {
        val eldest = idle.removeFirst().sandbox
        keys.remove(eldest)
        evicted.add(eldest)
      }
    }
    evicted.forEach { it.shutdown() }
  }

  /** Shuts down all sandboxes of the pool. */
  fun shutdown() {
    val sandboxes =
      synchronized(this) {
        keys.keys.toList().also {
          keys.clear()
          idle.clear()
        }
      }
    sandboxes.filterNot { it.isShutdown }.forEach { it.shutdown() }
  }

  private companion object {
    // Tests of one SDK can need differently configured sandboxes.
    private const val SANDBOXES_PER_SDK = 3
  }
}
