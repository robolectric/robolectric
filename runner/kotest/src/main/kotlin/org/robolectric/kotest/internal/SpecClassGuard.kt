package org.robolectric.kotest.internal

import io.kotest.core.spec.Spec
import java.lang.invoke.MethodHandles
import java.net.JarURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name

/**
 * Defines the spec classes of a class loader with guarded static initializers, see
 * [InitializerGuard], before something loads them as they are.
 *
 * A class is defined after the spec classes that it extends, and next to a loaded class of its
 * package: a class can only be defined from within its package.
 */
internal class SpecClassGuard(private val loader: ClassLoader) {
  private val files = SpecClassFiles(loader)

  // The classes that are settled, and with false those that are being settled.
  private val settled = HashMap<String, Boolean>()

  /** Guards those of the classes, given by name, that are spec classes and not loaded yet. */
  @Synchronized
  fun guard(names: Sequence<String>) {
    // A class that can't be guarded is left as it is, for Kotest to initialize as before.
    names.forEach { name -> runCatching { settle(name.replace('.', '/')) } }
  }

  /** Guards the spec classes in the directories of the class path. */
  fun guardDirectories() {
    guard(files.namesInDirectories())
  }

  /**
   * Defines the class if it is a spec class that needs a guard. Returns whether the class can be
   * loaded now, which it can't while a class that it extends is being settled.
   */
  private fun settle(name: String): Boolean {
    val known = settled[name]
    val file = if (known == null && files.isSpec(name)) files.fileOf(name) else null
    if (file == null) {
      return known ?: true.also { settled[name] = true }
    }
    settled[name] = false
    val canLoad = file.superName?.let { settle(it) } ?: true
    if (canLoad) {
      val guarded = InitializerGuard.guard(file.bytes())
      if (guarded != null) {
        anchorOf(name, file)?.let { define(guarded, it) }
      }
      settled[name] = true
    } else {
      settled.remove(name)
    }
    return canLoad
  }

  /**
   * Returns a loaded class of the package of the class, to define the class next to: a class that
   * is nested in it if there is one, such as that of a companion object.
   */
  private fun anchorOf(name: String, file: SpecClassFiles.ClassFile): Class<*>? =
    siblingsOf(name, file.url)
      .filter { it != name }
      .sortedWith(compareByDescending<String> { it.startsWith("$name$") }.thenBy { it })
      .firstNotNullOfOrNull { if (settle(it)) load(it) else null }

  /** Returns the names of the classes of the package of the class, where its class file is. */
  private fun siblingsOf(name: String, url: URL): List<String> {
    val prefix = name.substringBeforeLast('/', "").let { if (it.isEmpty()) it else "$it/" }
    val fileNames =
      when (url.protocol) {
        "file" ->
          Files.list(Paths.get(url.toURI()).parent).use { files -> files.map { it.name }.toList() }
        "jar" ->
          (url.openConnection() as JarURLConnection)
            .jarFile
            .entries()
            .asSequence()
            .filter { it.name.startsWith(prefix) && '/' !in it.name.substring(prefix.length) }
            .map { it.name.substring(prefix.length) }
            .toList()
        else -> emptyList()
      }
    return fileNames.filter { it.endsWith(CLASS) }.map { prefix + it.removeSuffix(CLASS) }
  }

  private fun load(name: String): Class<*>? = runCatching {
    Class.forName(name.replace('/', '.'), false, loader)
  }
    .getOrNull()

  /** Defines a class in the class loader and the package of the anchor, if it can be. */
  private fun define(bytes: ByteArray, anchor: Class<*>) {
    try {
      MethodHandles.privateLookupIn(anchor, MethodHandles.lookup()).defineClass(bytes)
    } catch (_: LinkageError) {
      // The class is loaded already.
    } catch (_: IllegalAccessException) {
      // The package of the class isn't open to this module.
    }
  }

  companion object {
    private const val CLASS = ".class"

    /** For the classes that Kotest loads: those of its own class loader. */
    val KOTEST: SpecClassGuard by lazy { SpecClassGuard(Spec::class.java.classLoader) }
  }
}
