package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.javadoc.Javadoc

/**
 * Modified from https://github.com/nebula-plugins/gradle-aggregate-javadocs-plugin.
 *
 * The origin license is Apache v2:
 * https://github.com/nebula-plugins/gradle-aggregate-javadocs-plugin?tab=Apache-2.0-1-ov-file#readme.
 */
class AggregateJavadocPlugin implements Plugin<Project> {
    private static final String AGGREGATE_JAVADOCS_TASK_NAME = 'aggregateJavadocs'

    @Override
    void apply(Project project) {
        Project rootProject = project.rootProject
        // This plugin only works for root project.
        if (project != rootProject) {
            return
        }
        rootProject.gradle.projectsEvaluated {
            Set<Project> javaSubprojects = getJavaSubprojects(rootProject)
            if (!javaSubprojects.isEmpty()) {
                rootProject.tasks.register(AGGREGATE_JAVADOCS_TASK_NAME, Javadoc) {
                    description = 'Aggregates Javadoc API documentation of all subprojects.'
                    group = JavaBasePlugin.DOCUMENTATION_GROUP

                    dependsOn javaSubprojects.javadoc
                    source javaSubprojects.javadoc.source

                    String buildDirectory = rootProject.layout.buildDirectory.get().asFile.path
                    destinationDir rootProject.file("$buildDirectory/docs/javadoc")
                    classpath = rootProject.files(javaSubprojects.javadoc.classpath)
                }
            }
        }
    }

    private static Set<Project> getJavaSubprojects(Project rootProject) {
        rootProject.subprojects.findAll { subproject -> subproject.plugins.hasPlugin(JavaPlugin) }
    }
}