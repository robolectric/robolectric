package org.robolectric.gradle

import java.net.URI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
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
    project.pluginManager.apply("signing")
    project.pluginManager.apply("java-library")
    project.pluginManager.apply("maven-publish")

    val projectVersion = project.version.toString()
    val isSnapshotVersion = projectVersion.endsWith("-SNAPSHOT")
    val mavenArtifactName = project.path.substring(1).split(":").joinToString("-")

    project.extensions.configure<BasePluginExtension> {
      // For Maven local install
      archivesName.set(mavenArtifactName)
    }

    project.extensions.configure<JavaPluginExtension> {
      val sourcesJar =
        project.tasks.register<Jar>("sourcesJar") {
          dependsOn(project.tasks.named("classes"))
          archiveClassifier.set("sources")
          from(sourceSets.getByName("main").allJava)
        }

      val javadocJar =
        project.tasks.register<Jar>("javadocJar") {
          val javadocTask = project.tasks.withType<Javadoc>()

          dependsOn(javadocTask)
          archiveClassifier.set("javadoc")
          from(javadocTask.map { it.destinationDir })
        }

      project.tasks.withType<Javadoc> {
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

      project.extensions.configure<PublishingExtension> {
        publications {
          register<MavenPublication>("mavenJava") {
            val skipJavadoc = System.getenv("SKIP_JAVADOC") == "true"

            from(project.components.getByName("java"))

            artifact(sourcesJar)
            if (!skipJavadoc) {
              artifact(javadocJar)
            }

            artifactId = mavenArtifactName

            pom {
              name.set(project.name)
              description.set("An alternative Android testing framework.")
              url.set("http://robolectric.org")

              licenses {
                license {
                  name.set("The MIT License")
                  url.set("https://opensource.org/licenses/MIT")
                }
              }

              scm {
                url.set("git@github.com:robolectric/robolectric.git")
                connection.set("scm:git:git://github.com/robolectric/robolectric.git")
                developerConnection.set("scm:git:https://github.com/robolectric/robolectric.git")
              }

              developers {
                developer {
                  name.set("Brett Chabot")
                  email.set("brettchabot@google.com")
                  organization.set("Google Inc.")
                  organizationUrl.set("http://google.com")
                }

                developer {
                  name.set("Michael Hoisie")
                  email.set("hoisie@google.com")
                  organization.set("Google Inc.")
                  organizationUrl.set("http://google.com")
                }

                developer {
                  name.set("Christian Williams")
                  email.set("antixian666@gmail.com")
                }
              }
            }
          }
        }

        repositories {
          maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"

            url = if (isSnapshotVersion) URI(snapshotsRepoUrl) else URI(releasesRepoUrl)

            credentials {
              username = System.getProperty("sonatype-login", System.getenv("SONATYPE_LOGIN"))
              password = System.getProperty("sonatype-password", System.getenv("SONATYPE_PASSWORD"))
            }
          }
        }

        project.extensions.configure<SigningExtension> {
          setRequired { !isSnapshotVersion && project.gradle.taskGraph.hasTask("uploadArchives") }

          sign(publications.getByName("mavenJava"))
        }
      }
    }
  }
}
