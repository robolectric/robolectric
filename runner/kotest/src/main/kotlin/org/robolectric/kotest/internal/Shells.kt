package org.robolectric.kotest.internal

import io.kotest.core.spec.AbstractSpec
import io.kotest.core.spec.Spec
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import sun.reflect.ReflectionFactory

/**
 * Creates specs of the classes that Kotest discovered, for it to recognize: Kotest's own reporters
 * know a spec by its class, and fail for a spec of another one, which a spec that is created in the
 * sandbox is.
 *
 * Such a shell is created without running the constructor of its class, and of the classes of the
 * test, which may use Android: only that of the spec style of Kotest runs. It has no tests, state
 * or callbacks until [StandIns] gives it those of the instances in the sandbox.
 */
internal object Shells {
  private const val KOTEST = "io.kotest."

  // The functions that a spec configures itself with. What a class returns from them usually
  // doesn't depend on the state of a spec, so a shell can answer them too.
  private val CONFIGURES =
    setOf(
      "isolationMode",
      "testCaseOrder",
      "timeout",
      "invocationTimeout",
      "tags",
      "assertionMode",
      "testExecutionMode",
      "coroutineDispatcherFactory",
      "duplicateTestNameMode",
    )

  // Shells that are created ahead, see [instantiate].
  private val reserved = ConcurrentHashMap<Class<*>, Queue<AbstractSpec>>()

  /**
   * Returns a shell of the class of the spec, or null if the class can't have one: if Kotest would
   * call callbacks that the class overrides on the shell, outside of Android and without the state
   * of a spec, if the static initializers of the class fail outside of Android, where they run for
   * the shell, or if the shell wouldn't configure itself as the spec does.
   *
   * @param further how many more specs of the class Kotest may ask for, at most
   */
  fun create(specClass: Class<out Spec>, real: Spec, further: Int): AbstractSpec? {
    val hierarchy = generateSequence<Class<*>>(specClass) { it.superclass }
    val style = hierarchy.firstOrNull { it.name.startsWith(KOTEST) }
    val constructor = style?.declaredConstructors?.firstOrNull { it.parameterCount == 0 }
    // Kotest must not call code of a class that is left being initialized: on another thread,
    // that would wait forever.
    val isLeft = LauncherTakeover.leftInitializing(specClass)
    val canStandIn =
      style != null &&
        constructor != null &&
        AbstractSpec::class.java.isAssignableFrom(style) &&
        hierarchy.takeWhile { it !== style }.none { overrides(it, style, isLeft) }
    val shell = if (canStandIn) instantiate(specClass, constructor, further) else null
    return shell?.takeIf {
      runCatching { configurationOf(it) }.getOrNull() == configurationOf(real)
    }
  }

  /**
   * Creates a shell. A class that is left being initialized, see [LauncherTakeover], can only be
   * instantiated on the thread of the launcher, so its shells are created ahead there, for the
   * specs that Kotest asks for on another thread.
   */
  private fun instantiate(
    specClass: Class<out Spec>,
    constructor: Constructor<*>,
    further: Int,
  ): AbstractSpec? {
    val isLeft = LauncherTakeover.leftInitializing(specClass)
    if (isLeft && LauncherTakeover.isOnThread()) {
      val shells = reserved.getOrPut(specClass) { ConcurrentLinkedQueue() }
      repeat(further - shells.size) { allocate(specClass, constructor)?.let(shells::add) }
    }
    val takesReserved = isLeft && !LauncherTakeover.isOnThread()
    return if (takesReserved) reserved[specClass]?.poll() else allocate(specClass, constructor)
  }

  /** Creates an instance of the class with the constructor of its spec style only. */
  private fun allocate(specClass: Class<out Spec>, constructor: Constructor<*>): AbstractSpec? =
    try {
      ReflectionFactory.getReflectionFactory()
        .newConstructorForSerialization(specClass, constructor)
        .newInstance() as AbstractSpec
    } catch (_: LinkageError) {
      // The static initializers of the class need Android.
      null
    }

  /**
   * Returns whether the class overrides something of the spec style that Kotest calls back, or that
   * Kotest calls at all if nothing of the class may be called.
   */
  private fun overrides(type: Class<*>, style: Class<*>, nothingCallable: Boolean): Boolean =
    type.declaredMethods.any { method ->
      val isCalled = nothingCallable || method.name !in CONFIGURES || method.parameterCount > 0
      isCalled &&
        !Modifier.isStatic(method.modifiers) &&
        !method.isSynthetic &&
        style.methods.any {
          it.name == method.name && it.parameterTypes.contentEquals(method.parameterTypes)
        }
    }

  /** Returns what the spec returns from the functions that it configures itself with. */
  private fun configurationOf(spec: Spec): List<Any?> =
    listOf(
      spec.isolationMode(),
      spec.testCaseOrder(),
      spec.timeout(),
      spec.invocationTimeout(),
      spec.tags().map { it.name }.toSet(),
      spec.assertionMode(),
      spec.testExecutionMode(),
      spec.coroutineDispatcherFactory()?.javaClass?.name,
      spec.duplicateTestNameMode(),
    )
}
