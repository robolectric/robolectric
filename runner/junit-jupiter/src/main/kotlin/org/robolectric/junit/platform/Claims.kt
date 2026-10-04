package org.robolectric.junit.platform

import org.junit.platform.launcher.LauncherDiscoveryListener
import org.junit.platform.launcher.LauncherDiscoveryRequest

internal object Claims {
  private val classes = ThreadLocal.withInitial { mutableSetOf<String>() }
  private val discovered = ThreadLocal.withInitial { false }

  fun clear() {
    classes.get().clear()
    discovered.set(false)
  }

  fun claim(type: Class<*>) {
    classes.get().add(type.name)
  }

  fun complete() {
    discovered.set(true)
  }

  fun isClaimed(type: Class<*>): Boolean =
    discovered.get() &&
      (type.name in classes.get() ||
        (type.enclosingClass != null && isClaimed(type.enclosingClass)))
}

/** Scopes claims to one discovery so an excluded engine cannot hide tests. */
public class RobolectricDiscoveryListener : LauncherDiscoveryListener {
  override fun launcherDiscoveryStarted(request: LauncherDiscoveryRequest) {
    Claims.clear()
  }
}
