package org.robolectric.kotest.internal

import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain
import java.util.Collections
import java.util.Optional
import java.util.WeakHashMap
import org.objectweb.asm.ClassReader

/**
 * Guards the spec classes as a Java agent, as the JVM loads them: for a JVM that is started with
 * `-javaagent:` and the jar of this module, where [StaticInitializers] has nothing to hook into.
 */
internal object SpecClassAgent : ClassFileTransformer {
  private const val SANDBOX = "org.robolectric.internal.bytecode.SandboxClassLoader"
  private val GUARD = InitializerGuard::class.java.name.replace('.', '/')

  /** Whether the JVM was started with the agent. */
  @Volatile
  var isInstalled: Boolean = false
    private set

  // Set while a class is looked at, to leave alone the classes that this itself loads.
  private val busy = ThreadLocal.withInitial { false }

  // The class files of the class loaders, with none for those whose classes are left alone.
  private val files: MutableMap<ClassLoader, Optional<SpecClassFiles>> =
    Collections.synchronizedMap(WeakHashMap())

  /** Called by the JVM before the main class is loaded, with the arguments of the agent. */
  @JvmStatic
  @Suppress("UnusedParameter") // The JVM looks for a method with these parameters.
  fun premain(arguments: String?, instrumentation: Instrumentation) {
    // What guarding a class needs is loaded now, and not while a class is being loaded.
    runCatching {
      val loader = javaClass.classLoader
      SpecClassFiles(loader).fileOf(GUARD)?.bytes()?.let {
        guarded(loader, it)
        InitializerGuard.guard(it)
      }
    }
    instrumentation.addTransformer(this)
    isInstalled = true
  }

  override fun transform(
    loader: ClassLoader?,
    className: String?,
    classBeingRedefined: Class<*>?,
    protectionDomain: ProtectionDomain?,
    classfileBuffer: ByteArray,
  ): ByteArray? {
    if (loader == null || classBeingRedefined != null || busy.get()) {
      return null
    }
    busy.set(true)
    try {
      // A class that can't be guarded is left as it is.
      return runCatching { guarded(loader, classfileBuffer) }.getOrNull()
    } finally {
      busy.set(false)
    }
  }

  /** Returns the class with a guarded static initializer if it is a spec class that has one. */
  private fun guarded(loader: ClassLoader, bytes: ByteArray): ByteArray? {
    val files = filesOf(loader)
    val isSpec = files != null && ClassReader(bytes).superName?.let { files.isSpec(it) } == true
    return if (isSpec) InitializerGuard.guard(bytes) else null
  }

  /**
   * Returns the class files of the class loader, or null if its classes are left alone: those of a
   * sandbox, and those that can't see the guard.
   */
  private fun filesOf(loader: ClassLoader): SpecClassFiles? =
    files
      .getOrPut(loader) {
        val isSandbox =
          generateSequence<Class<*>>(loader.javaClass) { it.superclass }.any { it.name == SANDBOX }
        val found = SpecClassFiles(loader).takeIf { !isSandbox && it.fileOf(GUARD) != null }
        Optional.ofNullable(found)
      }
      .orElse(null)
}
