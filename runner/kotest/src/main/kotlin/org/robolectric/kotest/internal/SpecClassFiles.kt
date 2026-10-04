package org.robolectric.kotest.internal

import java.lang.ref.WeakReference
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipFile
import org.objectweb.asm.ClassReader

/**
 * The class files that a class loader finds, and which of them are those of spec classes: the
 * classes that extend one of Kotest's. It reads the files, and loads no class for it.
 */
internal class SpecClassFiles(loader: ClassLoader) {
  /** A class file, where the class loader finds it: in a directory or in a jar. */
  class ClassFile(val url: URL) {
    val superName: String? = ClassReader(bytes()).superName

    fun bytes(): ByteArray = url.openStream().use { it.readBytes() }
  }

  // Not what keeps the class loader from being unloaded.
  private val loader = WeakReference(loader)

  // Without a lock: a Java agent asks from the threads that are loading classes.
  private val files = ConcurrentHashMap<String, Optional<ClassFile>>()
  private val specs = ConcurrentHashMap<String, Boolean>()

  /** Returns whether the class with the internal name is a spec class. */
  fun isSpec(name: String): Boolean =
    name.startsWith(KOTEST_SPECS) ||
      specs.getOrPut(name) { fileOf(name)?.superName?.let { isSpec(it) } ?: false }

  /** Returns the class file of the class with the internal name, or null if there is none. */
  fun fileOf(name: String): ClassFile? =
    files
      .getOrPut(name) {
        val url = loader.get()?.getResource(name + CLASS)
        Optional.ofNullable(url?.let { runCatching { ClassFile(it) }.getOrNull() })
      }
      .orElse(null)

  /** Returns the class and the classes that it extends, as far as they are spec classes. */
  fun specsOf(name: String): Sequence<String> =
    generateSequence(name) { fileOf(it)?.superName }
      .takeWhile { !it.startsWith(KOTEST_SPECS) && isSpec(it) }

  /** Returns the names of the classes in the directories of the class path. */
  fun namesInDirectories(): Sequence<String> {
    val roots = loader.get()?.getResources("")?.asSequence().orEmpty()
    return roots.filter { it.protocol == "file" }.flatMap { namesIn(Paths.get(it.toURI())) }
  }

  companion object {
    private const val CLASS = ".class"
    private const val KOTEST_SPECS = "io/kotest/core/spec/"

    /**
     * Returns the names of the classes in a directory or a jar, apart from local and anonymous
     * classes, which nothing asks for by name.
     */
    fun namesIn(root: Path): List<String> {
      val files =
        if (Files.isDirectory(root)) {
          Files.walk(root).use { paths ->
            paths.map { root.relativize(it).joinToString("/") }.toList()
          }
        } else {
          ZipFile(root.toFile()).use { zip -> zip.entries().asSequence().map { it.name }.toList() }
        }
      return files
        .filter { it.endsWith(CLASS) }
        .map { it.removeSuffix(CLASS) }
        .filterNot { it.substringAfterLast('$', "").firstOrNull()?.isDigit() == true }
    }
  }
}
