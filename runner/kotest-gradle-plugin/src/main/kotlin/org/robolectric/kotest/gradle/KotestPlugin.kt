package org.robolectric.kotest.gradle

import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.JavaExec
import org.gradle.process.ExecOperations

/**
 * Lets the static initializers of Kotest specs that run in Robolectric use Android in the tasks of
 * Kotest's own Gradle plugin, whose launcher initializes the spec classes before Robolectric's
 * Kotest extension can guard them.
 *
 * Before such a task starts its JVM, the plugin has the extension write the guarded spec classes
 * into a directory, and puts it before the class path of the task.
 */
abstract class KotestPlugin
@Inject
constructor(private val exec: ExecOperations, private val objects: ObjectFactory) :
  Plugin<Project> {
  override fun apply(project: Project) {
    val guard = GuardSpecClasses(exec, objects)
    project.tasks.withType(JavaExec::class.java).configureEach {
      if (javaClass.name.startsWith(KOTEST_TASKS)) {
        doFirst(guard)
      }
    }
  }

  private companion object {
    // The package of the tasks of Kotest's plugin, to do without a dependency on it.
    const val KOTEST_TASKS = "io.kotest.framework.gradle."
  }
}

/** Writes the guarded spec classes of a task, and puts them before its class path. */
internal class GuardSpecClasses(
  private val exec: ExecOperations,
  private val objects: ObjectFactory,
) : Action<Task> {
  override fun execute(task: Task) {
    val classpath = (task as JavaExec).classpath
    // Only for tests that use the extension, which is what writes the classes.
    if (classpath.files.any(::hasWriter)) {
      val directory = File(task.temporaryDir, "guarded-spec-classes")
      exec.javaexec {
        this.classpath = classpath
        mainClass.set(WRITER)
        args(directory.path)
        // The JVM that the task runs the tests with can read their classes.
        task.javaLauncher.orNull?.let { executable = it.executablePath.asFile.absolutePath }
      }
      task.classpath = objects.fileCollection().from(directory, classpath)
    }
  }

  private fun hasWriter(file: File): Boolean =
    when {
      file.isDirectory -> File(file, WRITER_FILE).isFile
      file.name.contains(MODULE) && file.name.endsWith(".jar") ->
        ZipFile(file).use { it.getEntry(WRITER_FILE) != null }
      else -> false
    }

  private companion object {
    // By name, to do without a dependency on Robolectric.
    const val WRITER = "org.robolectric.kotest.internal.SpecClassWriter"
    const val WRITER_FILE = "org/robolectric/kotest/internal/SpecClassWriter.class"

    /** What the name of the jar of the extension contains. */
    const val MODULE = "kotest"
  }
}
