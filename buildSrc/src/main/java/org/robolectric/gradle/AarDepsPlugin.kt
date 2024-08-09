package org.robolectric.gradle

import com.android.SdkConstants.DOT_JAR
import com.android.SdkConstants.EXT_AAR
import com.android.SdkConstants.EXT_JAR
import com.android.SdkConstants.FD_JARS
import com.android.SdkConstants.FN_CLASSES_JAR
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.registerTransform
import org.gradle.kotlin.dsl.withType
import org.robolectric.gradle.agp.ExtractAarTransform

/** Resolve AAR dependencies into jars for non-Android projects. */
@Suppress("unused")
class AarDepsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.dependencies.registerTransform(ClassesJarExtractor::class) {
      parameters.projectName.set(project.name)
      from.attribute(ARTIFACT_TYPE_ATTRIBUTE, EXT_AAR)
      to.attribute(ARTIFACT_TYPE_ATTRIBUTE, EXT_JAR)
    }

    project.afterEvaluate {
      configurations.forEach { configuration ->
        // I suspect we're meant to use the org.gradle.usage attribute, but this works.
        if (configuration.name.endsWith("Classpath")) {
          configuration.attributes { attribute(ARTIFACT_TYPE_ATTRIBUTE, EXT_JAR) }
        }
      }
    }

    // Warn if any AARs do make it through somehow; there must be a Gradle configuration
    // that isn't matched above.
    project.tasks.withType<JavaCompile>().forEach { task ->
      task.doFirst {
        val aarFiles = findAarFiles(task.classpath)

        check(aarFiles.isEmpty()) { "AARs on classpath: " + aarFiles.joinToString("\n  ") }
      }
    }
  }

  private fun findAarFiles(files: FileCollection): List<File> {
    return files.files.filter { it.name.lowercase().endsWith(".$EXT_AAR") }
  }

  /** Extracts classes.jar from an AAR. */
  abstract class ClassesJarExtractor @Inject constructor() : ExtractAarTransform() {
    override fun transform(outputs: TransformOutputs) {
      val classesJarFile = AtomicReference<File>()
      val outJarFile = AtomicReference<File>()

      val transformOutputs =
        object : TransformOutputs {
          // This is the one that ExtractAarTransform calls.
          override fun dir(path: Any): File {
            // ExtractAarTransform needs a place to extract the AAR. We don't really need to
            // register this as an output, but it'd be tricky to avoid it.
            val dir = outputs.dir(path)

            // Also, register our jar file. Its name needs to be quasi-unique or IntelliJ
            // Gradle/Android plugins get confused.
            classesJarFile.set(File(File(dir, FD_JARS), FN_CLASSES_JAR))
            outJarFile.set(File(File(dir, FD_JARS), "$path$DOT_JAR"))
            outputs.file("$path/$FD_JARS/$path$DOT_JAR")

            return outputs.dir(path)
          }

          override fun file(path: Any): File {
            error("Shouldn't be called")
          }
        }

      super.transform(transformOutputs)

      classesJarFile.get().renameTo(outJarFile.get())
    }
  }
}
