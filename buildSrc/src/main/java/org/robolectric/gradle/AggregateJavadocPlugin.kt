package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.register

/**
 * Modified from https://github.com/nebula-plugins/gradle-aggregate-javadocs-plugin.
 *
 * The origin license is Apache v2:
 * https://github.com/nebula-plugins/gradle-aggregate-javadocs-plugin/blob/master/LICENSE.
 */
class AggregateJavadocPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val rootProject = project.rootProject
    // This plugin only works on the root project.
    if (project != rootProject) {
      return
    }

    rootProject.gradle.projectsEvaluated {
      val javadocTasks = getJavadocTasks(rootProject)
      if (javadocTasks.isNotEmpty()) {
        rootProject.tasks.register<Javadoc>(AGGREGATE_JAVADOCS_TASK_NAME) {
          description = "Aggregates Javadoc API documentation of all subprojects."
          group = JavaBasePlugin.DOCUMENTATION_GROUP

          dependsOn(javadocTasks)
          source(javadocTasks.map { it.source })

          val javadocDirectory = rootProject.layout.buildDirectory.dir("docs/javadoc").get().asFile

          setDestinationDir(javadocDirectory)
          classpath = rootProject.files(javadocTasks.map { it.classpath })
        }
      }
    }
  }

  private fun getJavadocTasks(project: Project): Set<Javadoc> {
    return project.getAllTasks(true).values.flatten().filterIsInstance<Javadoc>().toSet()
  }

  private companion object {
    private const val AGGREGATE_JAVADOCS_TASK_NAME = "aggregateJavadocs"
  }
}
