package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

public class AndroidProjectConfigPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.android.testOptions.unitTests.all {
            // TODO: DRY up code with RoboJavaModulePlugin...
            testLogging {
                exceptionFormat "full"
                showCauses true
                showExceptions true
                showStackTraces true
                showStandardStreams true
                events = ["failed", "skipped"]
            }

            minHeapSize = "2048m"
            maxHeapSize = "8192m"

            if (System.env['GRADLE_MAX_PARALLEL_FORKS'] != null) {
                maxParallelForks = Integer.parseInt(System.env['GRADLE_MAX_PARALLEL_FORKS'])
            }

            def forwardedSystemProperties = System.properties
                    .findAll { k,v -> k.startsWith("robolectric.") }
                    .collect { k,v -> "-D$k=$v" }
            jvmArgs = forwardedSystemProperties

            doFirst {
                if (!forwardedSystemProperties.isEmpty()) {
                    println "Running tests with ${forwardedSystemProperties}"
                }
            }
        }

        project.task('provideBuildClasspath', type: ProvideBuildClasspathTask) {
            File outDir = new File(project.buildDir, "generated/robolectric")
            outFile = new File(outDir, 'robolectric-deps.properties')

            project.android.sourceSets['test'].resources.srcDir(outDir)
        }

        project.afterEvaluate {
            project.tasks.forEach { task ->
                if (task.name.matches("process.*UnitTestJavaRes")) {
                    task.dependsOn "provideBuildClasspath"
                }
            }
        }
    }
}
