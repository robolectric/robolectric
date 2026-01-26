package org.robolectric.gradle

import com.android.SdkConstants.DOT_JAVA
import com.android.SdkConstants.FD_GENERATED
import com.android.SdkConstants.FD_MAIN
import com.android.SdkConstants.SRC_FOLDER
import java.io.File
import java.util.jar.JarFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType

class ShadowsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.pluginManager.apply("idea")

    val shadows = project.extensions.create<ShadowsPluginExtension>("shadows")

    project.dependencies.add("annotationProcessor", project.project(":processor"))

    // Write generated Java into its own dir. See https://github.com/gradle/gradle/issues/4956
    val generatedSrcDir =
      project.layout.buildDirectory.dir("$FD_GENERATED/$SRC_FOLDER/apt/$FD_MAIN")

    project.tasks.named<JavaCompile>("compileJava").configure {
      options.generatedSourceOutputDirectory.set(generatedSrcDir)

      doFirst {
        val jsonDocsDir = project.layout.buildDirectory.dir("docs/json").get().asFile
        val sdks = project.rootProject.layout.buildDirectory.file("sdks.txt").get().asFile

        with(options.compilerArgs) {
          add("-Aorg.robolectric.annotation.processing.jsonDocsEnabled=true")
          add("-Aorg.robolectric.annotation.processing.jsonDocsDir=$jsonDocsDir")
          add("-Aorg.robolectric.annotation.processing.shadowPackage=${shadows.packageName}")
          add("-Aorg.robolectric.annotation.processing.sdkCheckMode=${shadows.sdkCheckMode}")
          add("-Aorg.robolectric.annotation.processing.sdks=$sdks")
        }
      }
    }

    // Include the generated sources in the Javadoc jar
    project.tasks.named<Javadoc>("javadoc").configure { source(generatedSrcDir) }

    // Verify that we have the apt-generated files in our Javadoc and sources jars
    project.tasks.named<Jar>("javadocJar").configure {
      doLast {
        val shadowPackageNameDir = shadows.packageName.replace('.', '/')
        checkForFile(archiveFile.get().asFile, "$shadowPackageNameDir/Shadows.html")
      }
    }

    project.tasks.named<Jar>("sourcesJar").configure {
      from(generatedSrcDir)
      doLast {
        val shadowPackageNameDir = shadows.packageName.replace('.', '/')
        checkForFile(archiveFile.get().asFile, "$shadowPackageNameDir/Shadows$DOT_JAVA")
      }
    }

    var configAnnotationProcessing: List<Project> by project.rootProject.extra
    configAnnotationProcessing += project

    // Prevents sporadic compilation error:
    // 'Bad service configuration file, or exception thrown while constructing
    //  Processor object: javax.annotation.processing.Processor: Error reading
    //  configuration file'
    //
    // See https://discuss.gradle.org/t/gradle-not-compiles-with-solder-tooling-jar/7583/20
    project.tasks.withType<JavaCompile>().configureEach { options.isFork = true }
  }

  abstract class ShadowsPluginExtension {
    abstract var packageName: String
    var sdkCheckMode: String = "WARN"
  }

  private companion object {
    private fun checkForFile(jar: File, name: String) {
      val hasFile = JarFile(jar).entries().asSequence().any { it.name == name }

      check(hasFile) { "Missing file $name in $jar" }
    }
  }
}
