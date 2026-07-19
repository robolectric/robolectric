package org.robolectric.gradle

import com.android.SdkConstants.DOT_JAVA
import com.android.SdkConstants.FD_GENERATED
import com.android.SdkConstants.FD_MAIN
import com.android.SdkConstants.SRC_FOLDER
import java.io.File
import java.util.jar.JarFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.process.CommandLineArgumentProvider

class ShadowsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    if (project.path != ":processor") {
      project.evaluationDependsOn(":processor")
    }

    project.pluginManager.apply("idea")

    val shadows = project.extensions.create<ShadowsPluginExtension>("shadows")

    project.dependencies.add(
      "annotationProcessor",
      project.dependencyFactory.createProjectDependency(":processor"),
    )

    // Write generated Java into its own dir. See https://github.com/gradle/gradle/issues/4956
    val generatedSrcDir =
      project.layout.buildDirectory.dir("$FD_GENERATED/$SRC_FOLDER/apt/$FD_MAIN")

    project.tasks.named<JavaCompile>("compileJava").configure {
      options.generatedSourceOutputDirectory.set(generatedSrcDir)

      val jsonDocsDir = project.layout.buildDirectory.dir("docs/json")
      val sdksFile =
        project.layout.file(
          project.project(":processor").tasks.named("generateSdksFile").map {
            it.outputs.files.singleFile
          }
        )

      options.compilerArgumentProviders.add(
        ShadowPluginCompilerArgumentProvider(
          packageName = shadows.packageName,
          sdkCheckMode = shadows.sdkCheckMode,
          jsonDocsDir = jsonDocsDir,
          sdksFile = sdksFile,
        )
      )
    }

    // Include the generated sources in the Javadoc jar
    project.tasks.named<Javadoc>("javadoc").configure { source(generatedSrcDir) }

    // Verify that we have the apt-generated files in our Javadoc and sources jars
    val packageName = shadows.packageName
    project.tasks.named<Jar>("javadocJar").configure {
      doLast {
        val shadowPackageNameDir = packageName.get().replace('.', '/')
        checkForFile(archiveFile.get().asFile, "$shadowPackageNameDir/Shadows.html")
      }
    }

    project.tasks.named<Jar>("sourcesJar").configure {
      from(generatedSrcDir)
      doLast {
        val shadowPackageNameDir = packageName.get().replace('.', '/')
        checkForFile(archiveFile.get().asFile, "$shadowPackageNameDir/Shadows$DOT_JAVA")
      }
    }

    @Suppress("UNCHECKED_CAST")
    val configAnnotationProcessing =
      project.rootProject.extra["configAnnotationProcessing"] as List<Project>
    project.rootProject.extra["configAnnotationProcessing"] = configAnnotationProcessing + project

    // Prevents sporadic compilation error:
    // 'Bad service configuration file, or exception thrown while constructing
    //  Processor object: javax.annotation.processing.Processor: Error reading
    //  configuration file'
    //
    // See https://discuss.gradle.org/t/gradle-not-compiles-with-solder-tooling-jar/7583/20
    project.tasks.withType<JavaCompile>().configureEach { options.isFork = true }
  }

  abstract class ShadowsPluginExtension {
    abstract val packageName: Property<String>
    abstract val sdkCheckMode: Property<String>

    init {
      sdkCheckMode.convention("WARN")
    }
  }

  private class ShadowPluginCompilerArgumentProvider(
    @get:Input private val packageName: Provider<String>,
    @get:Input private val sdkCheckMode: Provider<String>,
    @get:OutputDirectory private val jsonDocsDir: Provider<Directory>,
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    private val sdksFile: Provider<RegularFile>,
  ) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> {
      return listOf(
        "-Aorg.robolectric.annotation.processing.jsonDocsEnabled=true",
        "-Aorg.robolectric.annotation.processing.jsonDocsDir=${jsonDocsDir.get().asFile}",
        "-Aorg.robolectric.annotation.processing.shadowPackage=${packageName.get()}",
        "-Aorg.robolectric.annotation.processing.sdkCheckMode=${sdkCheckMode.get()}",
        "-Aorg.robolectric.annotation.processing.sdks=${sdksFile.get().asFile}",
      )
    }
  }

  private companion object {
    private fun checkForFile(jar: File, name: String) {
      val hasFile = JarFile(jar).entries().asSequence().any { it.name == name }

      check(hasFile) { "Missing file $name in $jar" }
    }
  }
}
