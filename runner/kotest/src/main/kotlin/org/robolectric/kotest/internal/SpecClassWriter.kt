package org.robolectric.kotest.internal

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Writes the spec classes of a class path, guarded, into a directory, as a step of a build. Put
 * before the class path, the directory makes every launcher load them guarded, see
 * [InitializerGuard].
 */
internal object SpecClassWriter {
  @JvmStatic
  fun main(arguments: Array<String>) {
    val directory = Paths.get(arguments.single())
    directory.toFile().deleteRecursively()
    write(checkNotNull(javaClass.classLoader), directory)
  }

  /**
   * Writes the spec classes in the directories of the class path, and those that they extend, that
   * have a static initializer to guard. Returns their names.
   */
  fun write(loader: ClassLoader, directory: Path): Set<String> {
    val files = SpecClassFiles(loader)
    val specs = files.namesInDirectories().flatMap { files.specsOf(it) }.toSet()
    return specs.filterTo(LinkedHashSet()) { name ->
      val guarded = files.fileOf(name)?.bytes()?.let { InitializerGuard.guard(it) }
      if (guarded != null) {
        val file = directory.resolve("$name.class")
        Files.createDirectories(file.parent)
        Files.write(file, guarded)
      }
      guarded != null
    }
  }
}
