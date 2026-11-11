package org.robolectric.runner.common.internal

import java.util.IdentityHashMap
import org.robolectric.annotation.SQLiteMode
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.SandboxManager
import org.robolectric.plugins.SdkCollection
import org.robolectric.util.inject.Injector

/**
 * The sandboxes of the whole JVM, which sessions that share sandboxes and the test runner, through
 * [SharedSandboxManager], use: one for each [SandboxIdentity], so that the same classes are used
 * for it everywhere. As the test runner's sandbox manager does, it keeps a limited number of
 * sandboxes, the least recently used first to go, but not those that sessions have lent out.
 */
internal object SharedSandboxes {
  private class Entry(val sandbox: AndroidSandbox) {
    var leases = 0
  }

  // In order of use, least recently used first.
  private val entries = LinkedHashMap<SandboxIdentity, Entry>(INITIAL_CAPACITY, LOAD_FACTOR, true)
  private var maxSize = 0

  /**
   * Returns the sandbox for an identity, which [build] creates if there is none. A [lease] keeps it
   * until it is [release]d. Sandboxes are created one at a time, so that there is one for each
   * identity.
   */
  fun acquire(
    identity: SandboxIdentity,
    maxSize: Int,
    lease: Boolean,
    build: () -> AndroidSandbox,
  ): Sandboxes.Lease {
    val evicted: List<AndroidSandbox>
    val result: Sandboxes.Lease
    synchronized(this) {
      this.maxSize = maxOf(this.maxSize, maxSize)
      val existing = entries[identity]?.takeUnless { it.sandbox.isShutdown }
      val entry = existing ?: Entry(build()).also { entries[identity] = it }
      if (lease) {
        entry.leases++
      }
      result = Sandboxes.Lease(entry.sandbox, created = existing == null)
      evicted = evict(entry)
    }
    evicted.forEach { it.shutdown() }
    return result
  }

  /** Gives back a sandbox that [acquire] lent out. */
  fun release(sandbox: AndroidSandbox) {
    val evicted =
      synchronized(this) {
        entries.values.firstOrNull { it.sandbox === sandbox && it.leases > 0 }?.let { it.leases-- }
        evict(keep = null)
      }
    evicted.forEach { it.shutdown() }
  }

  /** Removes the least recently used sandboxes that are too many and not lent out. */
  private fun evict(keep: Entry?): List<AndroidSandbox> {
    val evicted = mutableListOf<AndroidSandbox>()
    val iterator = entries.values.iterator()
    while (entries.size > maxSize && iterator.hasNext()) {
      val entry = iterator.next()
      if (entry !== keep && entry.leases == 0) {
        iterator.remove()
        evicted.add(entry.sandbox)
      }
    }
    return evicted
  }

  private const val INITIAL_CAPACITY = 16
  private const val LOAD_FACTOR = 0.75f
}

/** Lends out the sandboxes of the JVM to a session, and gives back what it didn't by its end. */
internal class SharedSandboxPool(injector: Injector) : Sandboxes {
  private val builder = injector.getInstance(SandboxManager.SandboxBuilder::class.java)
  private val sdks = injector.getInstance(SdkCollection::class.java)
  private val maxSize = sdks.supportedSdks.size * SandboxPool.SANDBOXES_PER_SDK

  // How many times the session leased each sandbox that it didn't give back yet.
  private val leased = IdentityHashMap<AndroidSandbox, Int>()

  override fun acquire(identity: SandboxIdentity, sqliteMode: SQLiteMode.Mode): Sandboxes.Lease {
    val lease =
      SharedSandboxes.acquire(identity, maxSize, lease = true) {
        builder.build(
          identity.instrumentation,
          identity.sdk,
          sdks.maxSupportedSdk,
          identity.resourcesMode,
          sqliteMode,
        )
      }
    synchronized(this) { leased.merge(lease.sandbox, 1, Int::plus) }
    return lease
  }

  override fun release(sandbox: AndroidSandbox) {
    val wasLeased =
      synchronized(this) {
        val count = leased[sandbox] ?: return@synchronized false
        if (count == 1) leased.remove(sandbox) else leased[sandbox] = count - 1
        true
      }
    if (wasLeased) {
      SharedSandboxes.release(sandbox)
    }
  }

  override fun shutdown() {
    val remaining = synchronized(this) { leased.toMap().also { leased.clear() } }
    remaining.forEach { (sandbox, count) -> repeat(count) { SharedSandboxes.release(sandbox) } }
  }
}
