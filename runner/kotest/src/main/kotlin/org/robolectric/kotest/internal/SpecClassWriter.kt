package org.robolectric.kotest.internal

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Writes the spec classes of the class path, guarded, into the directory that is its only argument,
 * as a step of a build: those in the directories of the class path, and those that they extend,
 * that have a static initializer to guard. Put before the class path, the directory makes every
 * launcher load them guarded, see [InitializerGuard].
 */
internal object SpecClassWriter {
  @JvmStatic
  fun main(arguments: Array<String>) {
    val directory = Paths.get(arguments.single())
    directory.toFile().deleteRecursively()
    val files = SpecClassFiles(checkNotNull(javaClass.classLoader))
    val specs = files.namesInDirectories().flatMap { files.specsOf(it) }.toSet()
    for (name in specs) {
      val guarded = files.fileOf(name)?.bytes()?.let { InitializerGuard.guard(it) } ?: continue
      val file = directory.resolve("$name.class")
      Files.createDirectories(file.parent)
      Files.write(file, guarded)
    }
  }
}
