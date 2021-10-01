package org.robolectric.gradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class RoboJavaModulePlugin implements Plugin<Project> {
    Closure doApply = {
        apply plugin: "java-library"

        def skipErrorprone = System.getenv('SKIP_ERRORPRONE') == "true"
        if (!skipErrorprone) {
          apply plugin: "net.ltgt.errorprone"
          project.dependencies {
            errorprone("com.google.errorprone:error_prone_core:$errorproneVersion")
            errorproneJavac("com.google.errorprone:javac:$errorproneJavacVersion")
          }
        }

        apply plugin: AarDepsPlugin

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        tasks.withType(JavaCompile) { task ->
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8

            // Show all warnings except boot classpath
            configure(options) {
                if (System.properties["lint"] != null && System.properties["lint"] != "false") {
                    compilerArgs << "-Xlint:all"        // Turn on all warnings
                }
                compilerArgs << "-Xlint:-options"       // Turn off "missing" bootclasspath warning
                encoding = "utf-8"                      // Make sure source encoding is UTF-8
            }

            def noRebuild = System.getenv('NO_REBUILD') == "true"
            if (noRebuild) {
                println "[NO_REBUILD] $task will be skipped!"
                task.outputs.upToDateWhen { true }
                task.onlyIf { false }
            }
        }

        ext.mavenArtifactName = project.path.substring(1).split(/:/).join("-")

        task('provideBuildClasspath', type: ProvideBuildClasspathTask) {
            File outDir = project.sourceSets['test'].output.resourcesDir
            outFile = new File(outDir, 'robolectric-deps.properties')
        }

        tasks['test'].dependsOn provideBuildClasspath

        test {
            exclude "**/*\$*" // otherwise gradle runs static inner classes like TestRunnerSequenceTest$SimpleTest

            // TODO: DRY up code with AndroidProjectConfigPlugin...
            testLogging {
                exceptionFormat "full"
                showCauses true
                showExceptions true
                showStackTraces true
                showStandardStreams true
                events = ["failed", "skipped"]
            }

            minHeapSize = "1024m"
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
    }

    @Override
    void apply(Project project) {
        doApply.delegate = project
        doApply.resolveStrategy = Closure.DELEGATE_ONLY
        doApply()
    }
}
