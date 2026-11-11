package org.robolectric.kotest.internal

import java.nio.file.Paths
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.UniqueIdSelector
import org.junit.platform.launcher.LauncherDiscoveryListener
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.LauncherSession
import org.junit.platform.launcher.LauncherSessionListener

/**
 * Guards the spec classes before Kotest's runner for the JUnit Platform initializes them, which it
 * does outside of the sandbox when it discovers them: a static initializer that uses Android fails
 * there, and with it the discovery of all specs. See [InitializerGuard].
 *
 * The classes in the directories of the class path are guarded when a launcher session opens,
 * because a build tool may load those itself, and the classes that a discovery selects when it
 * starts.
 */
internal class StaticInitializers(private val guard: SpecClassGuard) :
  LauncherSessionListener, LauncherDiscoveryListener {
  constructor() : this(SpecClassGuard.KOTEST)

  override fun launcherSessionOpened(session: LauncherSession) {
    unlessGuarded { guard.guardDirectories() }
  }

  override fun launcherDiscoveryStarted(request: LauncherDiscoveryRequest) {
    unlessGuarded {
      val selected = request.getSelectorsByType(ClassSelector::class.java).map { it.className }
      // The identifier of a spec or of one of its tests names the spec class.
      val identified =
        request.getSelectorsByType(UniqueIdSelector::class.java).flatMap { selector ->
          selector.uniqueId.segments.map { it.value }
        }
      val scanned =
        request.getSelectorsByType(ClasspathRootSelector::class.java).flatMap {
          SpecClassFiles.namesIn(Paths.get(it.classpathRoot))
        }
      guard.guard((selected + identified + scanned).asSequence())
    }
  }

  /** Whatever goes wrong leaves the classes as they are, for Kotest to initialize as before. */
  private fun unlessGuarded(guard: () -> Unit) {
    // A Java agent guards the classes as they are loaded.
    if (!SpecClassAgent.isInstalled) {
      runCatching(guard)
    }
  }
}
