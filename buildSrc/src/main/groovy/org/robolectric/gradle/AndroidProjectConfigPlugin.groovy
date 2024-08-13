package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class AndroidProjectConfigPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
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
            maxHeapSize = "12288m"

            if (System.env['GRADLE_MAX_PARALLEL_FORKS'] != null) {
                maxParallelForks = Integer.parseInt(System.env['GRADLE_MAX_PARALLEL_FORKS'])
            }

            def forwardedSystemProperties = System.properties
                    .findAll { k, v -> k.startsWith("robolectric.") }
                    .collect { k, v -> "-D$k=$v" }
            jvmArgs = forwardedSystemProperties
            jvmArgs += [
                    '--add-opens=java.base/java.lang=ALL-UNNAMED',
                    '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
                    '--add-opens=java.base/java.io=ALL-UNNAMED',
                    '--add-opens=java.base/java.net=ALL-UNNAMED',
                    '--add-opens=java.base/java.nio=ALL-UNNAMED', // required for ShadowVMRuntime
                    '--add-opens=java.base/java.security=ALL-UNNAMED',
                    '--add-opens=java.base/java.text=ALL-UNNAMED',
                    '--add-opens=java.base/java.util=ALL-UNNAMED',
                    '--add-opens=java.desktop/java.awt.font=ALL-UNNAMED',
                    '--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED',
                    '--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED',
                    '--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED',
            ]

            doFirst {
                if (!forwardedSystemProperties.isEmpty()) {
                    println "Running tests with ${forwardedSystemProperties}"
                }
            }
        }

        project.task('provideBuildClasspath', type: ProvideBuildClasspathTask) {
            File outDir = project.layout.buildDirectory.dir("generated/robolectric").get().asFile
            outFile = new File(outDir, "robolectric-deps.properties")

            project.android.sourceSets['test'].resources.srcDir(outDir)
        }

        project.afterEvaluate {
            project.tasks.forEach { task ->
                if (task.name.matches("process.*UnitTestJavaRes")) {
                    task.dependsOn "provideBuildClasspath"
                }
            }
        }

        // Only run tests in the debug variant. This is to avoid running tests twice when `./gradlew test` is run at the top-level.
        project.tasks.withType(Test).configureEach {
            onlyIf { variantName.toLowerCase().contains('debug') }
        }
    }
}

