package org.robolectric.gradleplugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies

class GradlePlugin implements Plugin<Project> {
    public static final String downloadTaskName = "robolectricDownloadAndroidSdks"

    static class Config {
        Object sdks
    }

    @Override
    public void apply(Project project) {
        Config config = new Config()
        project.extensions.add("robolectric", config)

        provideSdks(project)
        addRobolectricDependencies(project)
        enableIncludeAndroidResources(project)
    }

    private addRobolectricDependencies(Project project) {
        String robolectricVersion = loadResourceFile("robolectric-version.txt")
        def selfTestClassPath = System.getenv("gradle-robolectric-plugin.classpath")

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
    }

    private enableIncludeAndroidResources(Project project) {
        project.afterEvaluate { p ->
            Object androidExt = project.getExtensions().findByName("android")
            if (androidExt == null) {
                throw new GradleException("this isn't an android project?")
            }

            androidExt.testOptions.unitTests.includeAndroidResources = true
        }
    }

    void provideSdks(Project project) {
        def downloadTask = project.getTasks().create(downloadTaskName, DownloadAndroidSdks.class)
        project.tasks.add(downloadTask)
        project.repositories.add(project.repositories.jcenter())
    }

    String loadResourceFile(String name) {
        def resource = GradlePlugin.class.classLoader.getResource(name)
        if (resource == null) throw new IllegalStateException("$name not found")
        return resource.text
    }
}
