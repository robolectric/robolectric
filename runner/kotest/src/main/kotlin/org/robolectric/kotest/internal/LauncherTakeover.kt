package org.robolectric.kotest.internal

import io.kotest.core.spec.Spec
import io.kotest.engine.launcher.SpecScanner
import io.kotest.engine.launcher.main
import org.robolectric.kotest.RobolectricSpec

/**
 * Guards the spec classes with Kotest's own launcher, which initializes them before anything else.
 * It runs when the first spec class that implements [RobolectricSpec] is initialized, before the
 * static initializer of that class.
 *
 * The spec classes that are not loaded yet are guarded as usual. The one that is being initialized
 * can't be, so if it has a static initializer, this runs Kotest's launcher again instead of
 * returning to it. In that run the class counts as initialized on the thread of the launcher, and
 * its static initializer never runs outside of the sandbox.
 *
 * Another thread that needs that class to be initialized would wait forever, so [Shells] only
 * instantiates it on the thread of the launcher.
 */
internal object LauncherTakeover {
  private const val LAUNCHER = "io.kotest.engine.launcher."
  private const val LAUNCHER_MAIN = LAUNCHER + "MainKt"
  private const val LOADS = "java.lang.Class"
  private const val SCAN = "scan"

  // The classes that the launcher was initializing when it was run again, and its thread.
  @Volatile private var initializing: Set<Class<*>> = emptySet()
  @Volatile private var thread: Thread? = null

  /** Returns whether the class is left being initialized, by the thread of the launcher. */
  fun leftInitializing(type: Class<*>): Boolean = type in initializing

  /** Returns whether this is the thread that can use the classes that are being initialized. */
  fun isOnThread(): Boolean = Thread.currentThread() === thread

  /** Called when the first spec class that implements [RobolectricSpec] is initialized. */
  fun specClassInitializes() {
    val loader = Spec::class.java.classLoader
    // A sandbox loads this class again, and a Java agent guards the classes as they are loaded.
    if (javaClass.classLoader === loader && !SpecClassAgent.isInstalled) {
      runCatching { SpecClassGuard.KOTEST.guardDirectories() }
      val arguments = launcherArguments()
      val types = arguments?.let { runCatching { initializedBy(it, loader) }.getOrNull() }.orEmpty()
      // An object is what its static initializer creates, so that has to run.
      if (types.none(ObjectSpecs::isObject) && types.any { hasInitializer(it, loader) }) {
        initializing = types.toSet()
        thread = Thread.currentThread()
        main(checkNotNull(arguments).toTypedArray())
        error("Kotest's launcher returned, which ends the JVM when the specs are done")
      }
    }
  }

  /** Returns the arguments of Kotest's launcher if it is what initializes the spec class. */
  private fun launcherArguments(): List<String>? {
    val callers = Thread.currentThread().stackTrace.dropWhile { it.className != LOADS }
    val loadedBy = callers.firstOrNull { it.className != LOADS }?.className.orEmpty()
    // The main class and its arguments, which the launcher splits at spaces too.
    val command = System.getProperty("sun.java.command").orEmpty().split(' ')
    return command.drop(1).takeIf { loadedBy.startsWith(LAUNCHER) && command[0] == LAUNCHER_MAIN }
  }

  /**
   * Returns the classes that the launcher is initializing: the first of its spec classes, which it
   * initializes in their order, that implements [RobolectricSpec], with the classes that it extends
   * and that implement it too.
   */
  private fun initializedBy(arguments: List<String>, loader: ClassLoader): List<Class<*>> {
    val option = if ("--specs" in arguments) "--specs" else "--spec"
    val value = arguments.dropWhile { it != option }.drop(1).takeWhile { !it.startsWith("--") }
    val specs =
      if (value == listOf(SCAN)) SpecScanner.scan().map { it.java.name }
      else value.joinToString(" ").split(';')
    val first =
      specs
        .asSequence()
        .mapNotNull { runCatching { Class.forName(it, false, loader) }.getOrNull() }
        .firstOrNull { RobolectricSpec::class.java.isAssignableFrom(it) }
    return generateSequence(first) { it.superclass }
      .takeWhile { RobolectricSpec::class.java.isAssignableFrom(it) }
      .toList()
  }

  /** Returns whether the class has a static initializer that isn't guarded. */
  private fun hasInitializer(type: Class<*>, loader: ClassLoader): Boolean {
    val bytes = SpecClassFiles(loader).fileOf(type.name.replace('.', '/'))?.bytes()
    return bytes != null && InitializerGuard.guard(bytes) != null
  }
}
