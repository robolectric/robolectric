package org.robolectric.gradleplugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.tasks.testing.Test

class GradlePlugin implements Plugin<Project> {
    public static final String downloadTaskName = "robolectricDownloadAndroidSdks"

    @Override
    public void apply(Project project) {
        def selfTestClassPath = System.getenv("gradle-robolectric-plugin.classpath")
        def downloadTask = project.getTasks().create(downloadTaskName, DownloadAndroidSdks.class)
        def robolectricVersion = GradlePlugin.class.classLoader
                .getResource("robolectric-version.txt").text

        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            void beforeResolve(ResolvableDependencies resolvableDependencies) {
                def configuration = project.getConfigurations().getByName("testImplementation")

                Dependency dependency
                if (selfTestClassPath != null) {
                    def parts = selfTestClassPath.split(":")
                    dependency = project.getDependencies().create(project.files(*parts))
                } else {
                    dependency = project.getDependencies().create("org.robolectric:robolectric:$robolectricVersion")
                }
                configuration.getDependencies().add(dependency)

                project.getGradle().removeListener(this)
            }

            @Override
            public void afterResolve(ResolvableDependencies resolvableDependencies) {
            }
        })

        project.afterEvaluate { p ->
            Object androidExt = project.getExtensions().findByName("android")
            if (androidExt == null) {
                throw new GradleException("this isn't an android project?")
            }

            androidExt.testOptions.unitTests.includeAndroidResources = true

            // This needs to happen *after* the Android Gradle plugin creates tasks. Sigh.
            project.afterEvaluate {
                project.getTasks().withType(Test.class).forEach { task ->
                    if (task.name.endsWith("UnitTest")) {
                        task.dependsOn(downloadTask)
                    }
                }
            }
        }
    }
}
