package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

class DeployedRoboJavaModulePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.configureDeployedModule { sourceSets.getByName("main").allJava }
  }
}

/**
 * Configures Maven publishing for a deployed module.
 *
 * [sourcesSelector] picks the [SourceDirectorySet] to package in the sources JAR. Pure-Java modules
 * use `allJava`; modules with Kotlin sources use `allSource` so that `.kt` files are included.
 */
internal fun Project.configureDeployedModule(
  sourcesSelector: JavaPluginExtension.() -> SourceDirectorySet
) {
  pluginManager.apply("signing")
  pluginManager.apply("java-library")
  pluginManager.apply("maven-publish")

  val projectVersion = version.toString()
  val isSnapshotVersion = projectVersion.endsWith("-SNAPSHOT")
  val mavenArtifactName = path.substring(1).split(":").joinToString("-")

  extensions.configure<JavaPluginExtension> {
    val sourcesJar =
      tasks.register<Jar>("sourcesJar") {
        dependsOn(tasks.named("classes"))
        archiveClassifier.set("sources")
        from(sourcesSelector())
      }

    val javadocJar =
      tasks.register<Jar>("javadocJar") {
        val javadocTask = tasks.withType<Javadoc>()

        dependsOn(javadocTask)
        archiveClassifier.set("javadoc")
        from(javadocTask.map { it.destinationDir })
      }

    tasks.withType<Javadoc> {
      isFailOnError = false
      source = sourceSets.getByName("main").allJava

      val extraNavItem =
        """
        <ul class="navList">
            <li>Robolectric $projectVersion | <a href="/">Home</a></li>
        </ul>
        """
          .trimIndent()
      val javadocOptions = options as StandardJavadocDocletOptions
      javadocOptions.noTimestamp(true)
      javadocOptions.header = extraNavItem
      javadocOptions.footer = extraNavItem
    }

    extensions.configure<PublishingExtension> {
      publications {
        register<MavenPublication>("mavenJava") {
          val skipJavadoc = System.getenv("SKIP_JAVADOC") == "true"

          from(components.getByName("java"))

          artifact(sourcesJar)
          if (!skipJavadoc) {
            artifact(javadocJar)
          }

          artifactId = mavenArtifactName

          applyPomMetadata(project)
        }
      }

      sonatypeRepositories(isSnapshotVersion)

      extensions.configure<SigningExtension> {
        setRequired { !isSnapshotVersion && gradle.taskGraph.hasTask("uploadArchives") }

        sign(publications.getByName("mavenJava"))
      }
    }
  }
}
